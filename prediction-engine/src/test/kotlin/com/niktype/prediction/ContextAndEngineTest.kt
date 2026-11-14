package com.niktype.prediction

import com.niktype.engine.geometry.LanguageMode
import com.niktype.prediction.context.BigramModel
import com.niktype.prediction.context.PersistedModel
import com.niktype.prediction.context.UserModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BigramModelTest {

    @Test
    fun observeAndCount() {
        val m = BigramModel()
        m.observe("از", "این", 5)
        m.observe("از", "تو", 2)
        assertEquals(5, m.count("از", "این"))
        assertEquals(2, m.count("از", "تو"))
        assertEquals(0, m.count("به", "من"))
        assertEquals(setOf("این", "تو"), m.nextCounts("از").keys)
    }

    @Test
    fun evictionKeepsHighestCounts() {
        val m = BigramModel(cap = 5)
        m.observe("a", "1", 1)
        m.observe("b", "1", 1)
        m.observe("c", "1", 9) // پرشمارش
        m.observe("d", "1", 1)
        m.observe("e", "1", 1)
        m.observe("f", "1", 1)
        m.observe("g", "1", 1)
        assertTrue("cap should trigger eviction", m.size() <= 6)
        assertTrue(m.count("c", "1") == 9)
    }

    @Test
    fun serializeRoundtrip() {
        val m = BigramModel()
        m.observe("از", "این", 4)
        m.observe("به", "من", 2)
        val lines = m.toLines()
        val m2 = BigramModel()
        m2.restore(lines)
        assertEquals(4, m2.count("از", "این"))
        assertEquals(2, m2.count("به", "من"))
    }
}

class UserModelTest {

    @Test
    fun observeIncrementsFreq() {
        val u = UserModel()
        u.observe("کتاب")
        u.observe("کتاب")
        u.observe("کتاب")
        assertEquals(3, u.freq("کتاب"))
        assertEquals(0, u.freq("مجله"))
    }

    @Test
    fun digitsAndUrlsAreNotLearned() {
        val u = UserModel()
        u.observe("۱۲۳")
        u.observe("ali/reza")
        assertEquals(0, u.size())
    }

    @Test
    fun prefixSearch() {
        val u = UserModel()
        u.observe("میخوام")
        u.observe("میخورم")
        val res = u.prefix("میخ", LanguageMode.FA, 10)
        assertEquals(2, res.size)
        assertTrue(res.all { it.user })
    }

    @Test
    fun serializeRoundtrip() {
        val u = UserModel()
        u.observe("سلام")
        u.observe("سلام")
        val lines = u.toLines()
        val u2 = UserModel()
        u2.restore(lines)
        assertEquals(2, u2.freq("سلام"))
    }
}

class MemoryLearningStore : com.niktype.prediction.context.LearningStore {
    var model: PersistedModel? = null
    override fun load(): PersistedModel? = model
    override fun save(model: PersistedModel) { this.model = model }
}

class PredictionEngineTest {

    @Test
    fun learningMakesWordKnown() {
        val engine = PredictionEngine()
        assertFalse(engine.isKnown("نیک‌تایپ", LanguageMode.FA))
        engine.learn("نیک‌تایپ", null, LanguageMode.FA)
        assertTrue(engine.isKnown("نیک‌تایپ", LanguageMode.FA))
    }

    @Test
    fun persistenceRoundtrip() {
        val store = MemoryLearningStore()
        val e1 = PredictionEngine(store)
        e1.learn("زرنگار", null, LanguageMode.FA)
        e1.learn("سلام", "از", LanguageMode.FA)
        e1.persist()
        assertTrue(store.model != null)

        val e2 = PredictionEngine(store)
        assertTrue(e2.isKnown("زرنگار", LanguageMode.FA))
        assertTrue(e2.nextWords("از", LanguageMode.FA).isNotEmpty() ||
            com.niktype.prediction.data.WordData.BIGRAMS_FA.any { it.first == "از" })
    }

    @Test
    fun suggestionsUseDictionaryAndColloquial() {
        val engine = PredictionEngine()
        assertTrue(engine.suggestions("سلام", LanguageMode.FA, null).isNotEmpty())
        assertTrue(engine.suggestions("میخ", LanguageMode.FA, null).contains("میخوام"))
        assertTrue(engine.suggestions("the", LanguageMode.EN, null).isNotEmpty())
    }

    @Test
    fun emojiSuggestions() {
        val engine = PredictionEngine()
        assertTrue(engine.emojiFor("خندیدن").contains("😂"))
        assertTrue(engine.emojiFor("laugh").contains("😂"))
        assertTrue(engine.emojiFor("xyzq").isEmpty())
    }
}
