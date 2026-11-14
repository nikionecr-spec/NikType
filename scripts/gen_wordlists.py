#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""تولید فایل داده واژگان NikType (فارسی + انگلیسی + بی‌گرام + ایموجی)"""
import io, os

OUT = os.path.join(os.path.dirname(__file__), "..", "prediction-engine", "src", "main", "kotlin", "com", "niktype", "prediction", "data", "WordData.kt")

ZWNJ = "\u200c"

def k(s):
    """امن‌سازی رشته برای خروجی کاتلین (نیم‌فاصله با escape)."""
    return s.replace(ZWNJ, "\\u200C").replace('"', '\\"').replace("\\u200C", "\\u200C")

# ---------------------------------------------------------------------------
# فارسی — مرتب بر اساس اهمیت/بسامد تقریبی
# ---------------------------------------------------------------------------
FA = """و در به از این آن که را با برای است هست نیست بود شد شده می تا هم یا اما اگر چون هر هیچ
چند یک دو سه چهار پنج شش هفت هشت نه ده صد هزار میلیون من تو او ما شما آنها ایشان خود همین همان
کجا کی چرا چه چطور چقدر کدام همه بعضی خیلی کم زیاد بیشتر کمتر بله خیر آره شاید حتما باید نباید
پس خب البته فقط باز هنوز حالا الان بعد قبل بعدا همیشه اغلب گاهی هرگز سلام خداحافظ بدرود تشکر ممنون متشکرم لطفا ببخشید عذر معذرت تبریک وقت زندگی کار خانه شغل
مدرسه دانشگاه کتاب خیابان شهر کشور دنیا روز شب صبح ظهر عصر سال ماه هفته امروز دیروز فردا ساعت
دقیقه ثانیه زمستان بهار تابستان پاییز کرد کردم کردی کردیم کردید کردند کرده رفت رفتی رفتیم رفتند
می‌روم می‌روی می‌رود می‌رویم می‌روند می‌آیم می‌آیی می‌آید آمد آمدی آمدند بیا برو بخور ببین بگو
بنویس بخوان بشین پاشو بگیر بده بزن بخر کن نکن دید دیدم دیدی دیدیم دیدند می‌بینم می‌بینی می‌بیند
می‌بینیم گفتم گفتی گفت گفتیم گفتند می‌گویم شنید شنیدم شنیدی شنیدند می‌شنوم می‌شنوی می‌شنوند
دانست دانستم می‌دانم می‌دانی می‌دانند خواست خواستم خواستی خواستند می‌خواهم می‌خواهی می‌خواهد
می‌خواهیم توانست توانستم می‌توانم می‌توانی می‌توانند نوشت نوشتم نوشتی نوشتند می‌نویسم می‌نویسی
می‌نویسند خواند خواندم خواندی خواندند می‌خوانم می‌خوانی می‌خوانند خورد خوردی خوردیم خوردند
می‌خورم می‌خوری می‌خورند خوابید خوابیدم می‌خوابم نشست نشستم می‌نشینم ساخت ساختم ساختند می‌سازم
آورد آوردم آوردند گرفتم گرفتی گرفتند می‌گیرم می‌گیری می‌گیرند داد دادم دادی دادیم دادند می‌دهم
می‌دهی می‌دهد می‌دهند خرید خریدم خریدی خریدند می‌خرم می‌خری فروخت افتاد افتادند برد بردی بردند
می‌برم می‌برند گذاشت گذاشتم می‌گذارم ماند ماندیم می‌ماند برگشت برگشتم برمی‌گردم برمی‌گردیم شروع
پایان تمام دارد ندارد دارم داریم دارید دارند داری داشت داشتم داشتی داشتیم داشتند هستم هستی
هستیم هستید هستند بودم بودی بودیم بودید بودند آدم مردم خانواده پدر مادر برادر خواهر پدربزرگ
مادربزرگ بچه فرزند پسر دختر همسر شوهر زن مرد آقا خانم دوست رفیق همسایه مهمان همکار رئیس کارمند
معلم استاد دانش‌آموز دانشجو پزشک دکتر پرستار مهندس جهان جامعه قانون دولت ملت خبر اخبار روزنامه
برنامه مسئله مشکل راه‌حل پیشنهاد انتخاب تصمیم اجازه مقایسه تفاوت نتیجه علت مهم ضروری لازم کافی
ممکن ساده پیچیده درست غلط اشتباه واقعی حقیقت کلمه جمله متن ایده فکر اندیشه احساس عشق علاقه
دوستی غم شادی خنده گریه ترس امید آرزو خاطره یاد حافظه فهم هوش دانش آموزش درس امتحان نمره
سوال جواب معنی مثال دلیل هدف نقشه قدم سفر مسافر مقصد وقت زندگی کار خانه شغل مدرسه دانشگاه کتاب خیابان شهر کشور دنیا روز شب صبح ظهر عصر سال ماه هفته امروز دیروز فردا ساعت
دقیقه ثانیه زمستان بهار تابستان پاییز هدیه جایزه بازی ورزش فوتبال فیلم موسیقی آهنگ رادیو
تلویزیون گوشی موبایل تلفن کامپیوتر لپ‌تاپ اینترنت صفحه کلید قفل رمز پول قیمت هزینه خرید فروش
اجاره درآمد حقوق بانک پست فرودگاه ایستگاه اتوبوس مترو تاکسی ماشین اتاق آشپزخانه حمام دستشویی
حیاط باغ پارک رستوران کافه هتل بیمارستان داروخانه اداره شرکت کارخانه دفتر مسجد بازار مغازه
کوچه جاده مسیر اسم نام صدا رنگ شکل اندازه عدد شماره آدرس سمت جهت بالا پایین جلو عقب راست چپ
وسط کنار نزدیک دور بین روی زیر مقابل بیرون داخل خارج بدن سر صورت چشم گوش بینی دهان لب دندان
زبان گردن شانه دست انگشت ناخن پا زانو قلب خون استخوان مغز پوست مو آب هوا باد باران برف آفتاب
خورشید ماه زمین آسمان ستاره دریا رود کوه جنگل گل درخت برگ گیاه حیوان پرنده ماهی سگ گربه مرغ
گاو گوسفند اسب شیر غذا نان برنج گوشت مرغ سبزی میوه سیب پرتقال موز انگور هندوانه خیار گوجه پیاز
کاهو چای قهوه شیر شکر نمک فلفل روغن ماست پنیر کره تخم‌مرغ شیرینی بستنی کیک صبحانه ناهار شام
عسل سلامتی مریضی بیماری
درد تب دارو درمان مریض بیمار سالم خوشحال ناراحت خسته شاد عصبانی آرام نگران گرسنه تشنه عالی
خوب بهتر بهترین بد بدتر بدترین بزرگ بزرگتر بزرگترین کوچک کوچکترین بلند کوتاه سریع آهسته تازه
قدیمی نو جدید سخت آسان راحت گرم سرد خنک تاریک روشن تمیز کثیف زیبا زشت قوی ضعیف پر خالی امن
امنیت آزاد آزادی صلح جنگ آرامش سبک سنگین شیرین تلخ خوشمزه آخرین دیگر دیگران هرگز چیز هرکس عزیز
عزیزم قشنگ بامزه باحال امشب تا بار حالات حالتان متأسفانه متاسفانه راستش راستی جدی واقعا الکی
جدا خلاصه مثلا یعنی فعلا تقریبا معمولا اکثرا کاش حالا? عملا اصولا اصلا هنوز? هرگز? لطفا? ببین
فقط? درست? بابا خواهر مهم نیست? کنارهم می‌بینم ببینم بشود می‌شود می‌شوی می‌شنوم می‌آورم می‌آید
می‌آورند خوانده گرفته داده گذشته آمده رفته خورده دیده گفته شنیده علم
""".split()

COLLOQ = """خب باشه آره چی چیه کیه کجا کجایی کجاست کدوم چطوری جوری اونو اینو اینجا اونجا اون
دیگه بابا مامان بابام مامانم داداش دوستم خونمون خونه میخوام نمیخوام میخوای میخوایم میکنم
نمیکنم میکنی میکنن میگی نمیگم میگم میگه میگن میبینم میبینی میبینه میبینن میرم نمیرم میری میرن
میام میای میان میدم نمیدم میدی میده میدن میگیرم میگیری میگیره میذارم میذاری میذاره میذارن
میشینم میشینی میشینه میشینن میپزم میخورم میخوری میخورن میخوابم داری نداری هستم نیستی هستیم
نیستن بودم بودی بودیم بودن هستن نیستم نداریم ندارن میشه نمیشه میشناسی میشناسم میخونه میخونم
میخونیم مینویسم مینویسی مینویسه مینویسن میدونم نمیدونم میدونیم میدونن بگو بگم ببین بیا بیای
بیاین بشین بخر بخور بخورم بخوریم بخواب برم بری برن بیام بیای بیار بگیر بگیری بگیرن بده بدم
بزن بزنم بخون بخونم بنویس بنویسم بپرس همین همینه همونی یه یهو یعنی واسه چیزی هیچی بعضیا
همه‌شون همه‌چی گرون آسونه سخته راحته خوشمزه‌ست سلام خوبی خوبم مرسی چه‌خبر چی‌کار صبح‌بخیر
شب‌بخیر خسته‌نباشید دستت‌دردنکنه خداحافظ خدافظ فعلا میبخشید مبارک قشنگ عزیزم میبینمت امشب
میتونم میتونی میتونه میتونیم نمیتونم میتونن میزنم میزنی میزنه میزنن میارم میاری میاره میارن
میبرم میبری میبره میبرن میبینید میگید میکنید میخواید میدید میگیرید میرید چشم بهبه اوه وای
آهان خسته‌ام انشاءالله خدانگهدار ممنونم خیلی‌ممنون بابا? کجاست? چطورید یا? همونی? اینطوری
اونطوری چندتا یکی یکی‌ها تازه؟ لوس خنگ باهوش زرنگ شلوغ خلوت یخ پررو رفیق داداش? خواهرم
برادرش مامان‌بزرگ? اینجا? اونجا? بعدا? دیروز? صبحم شبم روزم شهرم خونم دلم دلتنگ تنگم گشنمه
تشنه‌ام خوشحالم ناراحتم خوشحال‌کننده? می‌خوام؟ باشه‌ها? حله تمومه تموم تمومش تموم شد؟
""".split()

EN = """the be to of and a in that have it for not on with he as you do at this but his by from
they we say her she or an will my one all would there their what so up out if about who get
which go me when make can like time no just him know take people into year your good some
could them see other than then now look only come its over think also back after use two how
our work first well way even new want because any these give day most us man woman child boy
girl friend family mother father brother sister person life world country city town street
school university book page word sentence letter name question answer story movie music song
game sport food water bread milk tea coffee fruit apple rice meat chicken vegetable sugar salt
dog cat bird horse fish tree flower garden park room door window wall table chair bed kitchen
bathroom money price cost buy sell pay job office company boss worker teacher student doctor
nurse hospital medicine sick health body hand eye ear nose mouth leg foot head face hair smile
laugh cry happy sad angry tired sleep morning evening night week month today tomorrow yesterday
monday tuesday wednesday thursday friday saturday sunday winter summer spring autumn rain snow
wind sun sky cloud star sea river mountain forest road car bus train plane phone message call
talk speak listen read write draw learn teach play run walk stop start open close wait help
love hate need hope dream idea problem solution example meaning reason goal plan step part
whole half number amount little much many few more less better best worse worst big small long
short tall old young nice beautiful ugly strong weak easy hard fast slow hot cold warm cool
wet dry clean dirty dark light high low near far inside outside above below behind front left
right center middle early late always never often sometimes usually again once twice still
really very too quite almost maybe probably surely actually such own same here where why when
ask tell explain show bring put keep lose find found send receive try fix wash cook eat drink
wake sit stand jump fly swim drive ride fall grow live die meet visit travel arrive leave
return stay remember forget understand believe feel touch move turn begin finish end continue
change build break happen become seem please thanks thank welcome sorry hi hello hey bye okay
fine great cool awesome sure kind type sort group team class level point line row column list
note paper file folder box bag package gift card key lock floor roof place address home house
building village farm land ground ocean beach island hill air breath smoke shadow color sound
voice noise silence water? night? thing things something anything nothing everything someone
anyone everyone yes both each ever about? again? once? soon course dont cant its im youre
ive youre? id ill youre? were theyre lets didnt doesnt isnt wasnt wont cant? didnt? just?
""".split()

# ---------------------------------------------------------------------------
# بی‌گرام بذر
# ---------------------------------------------------------------------------
BIGRAMS_FA = """از این از تو از من از ما از او از دست از راه از خانه از کار از بین از خود
به تو به من به ما به او به خانه به کار به دست به سمت به جای به خاطر
این است این که این کار این کتاب این جانبی اینجا خوبه
در این در آن در خانه در کار در شهر در مدرسه در پایان در کنار در مورد
که در که به که از که با که ما که تو که او که همه که این
را به را دارم را دیدم را گرفتم را دادم را گفت را میخواهم را نوشتم
است که است و است این بود که شد که شده است بود و
با من با تو با ما با او با این با آن با هم
برای این برای تو برای من برای ما برای کار برای او
من هستم من دارم من میروم من میخوام من نمیدونم تو هستی تو داری ما هستیم ما میرویم
شما هستید شما دارید آنها هستند ایشان هستند بچه‌ها هستند
سلام چطور سلام خوبی سلام عزیزم خوبم ممنون ممنون از خیلی ممنون خیلی خوب خیلی زیاد خیلی کم
چه خبر چطور هستی چطورید صبح بخیر شب بخیر خسته نباشید خداحافظ فعلا
میخوام برم میخوام بخورم میخوام ببینم میخوام که میخوای بیای میخوای بریم باید برم باید بگم
میتونی بیای میتونم ببینم الان میخوام فردا میبینمت امشب خونه
هر روز هر بار هرکس که یک بار یک روز دو تا چند تا همه چیز هیچ چیزی
این یکی اون یکی همینطوری اونطوری اینطوری
و این و آن و همه و یک و در و با فکر میکنم فکر کن فکرش را? نگاه کن نگاه میکنم
صبر کن ببینم صبر کن برو ببین بگو بهم بگو ببینم بگم
""".split("،".join([])) if False else """از این|از تو|از من|از ما|از او|از دست|از راه|از خانه|از کار|از بین|از خود
به تو|به من|به ما|به او|به خانه|به کار|به دست|به سمت|به جای|به خاطر
این است|این که|این کار|این کتاب|اینجا خوبه
در این|در آن|در خانه|در کار|در شهر|در مدرسه|در پایان|در کنار|در مورد
که در|که به|که از|که با|که ما|که تو|که او|که همه|که این
را به|را دارم|را دیدم|را گرفتم|را دادم|را گفت|را میخواهم|را نوشتم
است که|است و|است این|بود که|شد که|شده است|بود و
با من|با تو|با ما|با او|با این|با آن|با هم
برای این|برای تو|برای من|برای ما|برای کار|برای او
من هستم|من دارم|من میروم|من میخوام|من نمیدونم|تو هستی|تو داری|ما هستیم|ما میرویم
شما هستید|شما دارید|آنها هستند|ایشان هستند
سلام چطور|سلام خوبی|سلام عزیزم|خوبم ممنون|ممنون از|خیلی ممنون|خیلی خوب|خیلی زیاد|خیلی کم
چه خبر|چطور هستی|چطورید|صبح بخیر|شب بخیر|خسته نباشید|خداحافظ فعلا
میخوام برم|میخوام بخورم|میخوام ببینم|میخوام که|میخوای بیای|میخوای بریم|باید برم|باید بگم
میتونی بیای|میتونم ببینم|الان میخوام|فردا میبینمت|امشب خونه
هر روز|هر بار|یک بار|یک روز|دو تا|چند تا|همه چیز|هیچ چیزی
و این|و آن|و همه|و یک|و در|و با|فکر میکنم|فکر کن|نگاه کن|نگاه میکنم
صبر کن|ببینم صبر کن|بگو ببینم|ببینم بگم
"""

BIGRAMS_EN = """of the|in the|to the|on the|for the|and the|with the|from the|at the|by the
it is|this is|that is|there is|there are|he is|she is|it was|there was|i am|i have|i do|i can|i will
i want|i need|i know|i think|i like|i love|i see|i go|i get|i would|i should|i could|i did|i was
you are|you can|you will|you know|you want|you have|you do|we are|we have|we can|we will|we do
they are|they have|they do|can you|can i|do you|did you|would you|could you|will you|are you
what is|what are|what do|how are|how do|how much|how many|how long|when is|where is|why do
thank you|thanks for|good morning|good night|good luck|good job|see you|nice to|let me|let us
a lot|a few|a little|a bit|as well|at least|because of|because i|but i|but the|so that|so i|so much
too much|too many|of course|one of|one day|one more|some of|most of|all of|part of|out of|up to
close to|next to|just a|just for|my name|my friend|my family|my house|your name|your help
the same|the best|the first|the last|the next|the new|the most|in a|on a|come on|come back
come here|go to|go back|go home|look at|look for|talk about|think about|know about
no problem|no way|dont know|dont want|dont have|its a|im good|youre right
"""

def parse_pairs(text):
    out = []
    for line in text.strip().splitlines():
        for pair in line.split("|"):
            parts = pair.split()
            if len(parts) == 2:
                out.append((parts[0], parts[1], 3))
    return out

BIGRAMS_FA = parse_pairs(BIGRAMS_FA)
BIGRAMS_EN = parse_pairs(BIGRAMS_EN)

# ---------------------------------------------------------------------------
# ایموجی
# ---------------------------------------------------------------------------
EMOJI = {
    "خندیدن": ["😂", "🤣", "😄"], "خنده": ["😂", "🤣", "😄"], "قاهقه": ["🤣", "😂"],
    "خوشحال": ["😄", "😊", "🎉"], "شاد": ["😄", "🎉", "😊"], "لبخند": ["😊", "🙂", "😃"],
    "عشق": ["❤️", "😍", "🥰"], "عاشق": ["😍", "❤️", "🥰"], "دوستت": ["❤️", "😘", "🥰"],
    "قلب": ["❤️", "💖", "💙"], "دلم": ["🥺", "❤️", "💔"], "دلتنگ": ["🥺", "💔"],
    "گریه": ["😢", "😭", "🥲"], "ناراحت": ["😢", "😞", "💔"], "غمگین": ["😢", "😞", "😔"],
    "ترس": ["😨", "😱", "😰"], "عصبانی": ["😠", "😡", "🤬"], "تعجب": ["😲", "🤯", "😮"],
    "خجالت": ["😳", "🙈"], "خفن": ["😎", "🆒", "🔥"], "باهوش": ["😎", "🧠"],
    "ممنون": ["🙏", "💐", "😊"], "مرسی": ["🙏", "😊", "👍"], "متشکرم": ["🙏", "💐"],
    "ببخشید": ["🙏", "😔"], "معذرت": ["🙏", "😔"], "لطفا": ["🙏", "🤲"],
    "سلام": ["👋", "😊", "🤝"], "درود": ["👋", "🤝"], "خداحافظ": ["👋", "😢"],
    "خدافظ": ["👋"], "بدرود": ["👋"], "فعلا": ["👋", "✌️"],
    "خوب": ["👍", "✨", "🌟"], "عالی": ["👍", "🌟", "✅"], "باشه": ["👌", "✅", "👍"],
    "تایید": ["✅", "👍"], "فکر": ["🤔", "💭"], "سوال": ["❓", "🤔"], "چرا": ["🤔", "❓"],
    "غذا": ["🍕", "🍔", "🍜"], "گشنه": ["🍽️", "🍕", "😋"], "خوشمزه": ["😋", "😍", "👍"],
    "چای": ["☕", "🍵"], "قهوه": ["☕", "☕"], "شام": ["🍽️", "🍜", "😋"],
    "خواب": ["😴", "🌙", "💤"], "شب": ["🌙", "🌃", "😴"], "صبح": ["🌅", "☀️", "☕"],
    "کار": ["💼", "💻", "📊"], "موبایل": ["📱", "☎️"], "تلفن": ["📞", "📱"],
    "پول": ["💰", "💵", "🤑"], "هوا": ["🌤️", "🌡️"], "باران": ["🌧️", "☔", "⛈️"],
    "برف": ["❄️", "⛄", "🌨️"], "خورشید": ["☀️", "🌞", "🌤️"], "ستاره": ["⭐", "✨", "🌟"],
    "دریا": ["🌊", "🏖️", "⛵"], "کوه": ["⛰️", "🏔️"], "گل": ["🌹", "🌸", "🌷"],
    "تولد": ["🎂", "🎉", "🎈"], "جشن": ["🎉", "🥳", "🎊"], "مهمونی": ["🎉", "🥳", "🎊"],
    "تبریک": ["🎉", "👏", "🥳"], "مبارک": ["🎉", "🎊", "👏"], "عروسی": ["💍", "👰", "💐"],
    "ورزش": ["⚽", "🏀", "💪"], "فوتبال": ["⚽", "🥅", "🏟️"], "قوی": ["💪", "🦾"],
    "موسیقی": ["🎵", "🎶", "🎸"], "آهنگ": ["🎵", "🎧", "🎶"], "کتاب": ["📚", "📖", "🔖"],
    "درس": ["📖", "✏️", "🎓"], "مطالعه": ["📖", "📚"], "امتحان": ["📝", "📚", "😅"],
    "ماشین": ["🚗", "🚙", "🚘"], "سفر": ["🧳", "✈️", "🌍"], "هواپیما": ["✈️", "🛫"],
    "خانه": ["🏠", "🏡"], "دکتر": ["🩺", "🏥", "💊"], "مریض": ["🤒", "🤕", "💊"],
    "دارو": ["💊", "💉"], "خدا": ["🤲", "📿"], "دعا": ["🤲", "🕌", "📿"], "نماز": ["🕌", "📿", "🤲"],
    "دوست": ["🤝", "👥", "❤️"], "خانواده": ["👨‍👩‍👧‍👦", "❤️", "🏡"], "مادر": ["❤️", "🌷", "👩"],
    "پدر": ["👍", "❤️", "👨"], "بچه": ["👶", "🧒", "🍼"], "گربه": ["🐱", "🐈"],
    "سگ": ["🐶", "🐕"], "پرنده": ["🐦", "🕊️"], "ماهی": ["🐟", "🐠"], "گوسفند": ["🐑"],
    "درخت": ["🌳", "🌲"], "بارون": ["🌧️", "☔"], "امروز": ["📅"], "فردا": ["📆", "🌅"],
    "عزیزم": ["❤️", "😘", "🥰"], "جان": ["❤️", "🥰"], "خوشحال‌کننده": ["😄", "🎉"],
    "laugh": ["😂", "🤣", "😄"], "happy": ["😄", "😊", "🎉"], "sad": ["😢", "😞", "💔"],
    "love": ["❤️", "😍", "🥰"], "cry": ["😢", "😭"], "angry": ["😠", "😡"],
    "thanks": ["🙏", "😊"], "thank": ["🙏", "😊"], "hello": ["👋", "😊", "🤝"],
    "hi": ["👋", "😊"], "bye": ["👋", "✌️"], "good": ["👍", "✨"], "great": ["👍", "🌟", "🎉"],
    "ok": ["👌", "✅"], "okay": ["👌", "✅"], "yes": ["✅", "👍", "🙌"], "no": ["❌", "🙅"],
    "think": ["🤔", "💭"], "question": ["❓", "🤔"], "food": ["🍕", "🍔", "🍜"],
    "tasty": ["😋", "😍"], "coffee": ["☕"], "tea": ["🍵", "☕"], "sleep": ["😴", "🌙"],
    "night": ["🌙", "🌃"], "morning": ["🌅", "☀️"], "work": ["💼", "💻"],
    "phone": ["📱", "📞"], "money": ["💰", "💵"], "rain": ["🌧️", "☔"],
    "snow": ["❄️", "⛄"], "sun": ["☀️", "🌞"], "star": ["⭐", "✨"], "sea": ["🌊", "🏖️"],
    "flower": ["🌹", "🌸"], "birthday": ["🎂", "🎉", "🎈"], "party": ["🎉", "🥳", "🎊"],
    "congrats": ["🎉", "👏", "🥳"], "congratulations": ["🎉", "🎊", "👏"],
    "sport": ["⚽", "🏀", "💪"], "football": ["⚽", "🥅"], "music": ["🎵", "🎶", "🎧"],
    "book": ["📚", "📖"], "study": ["📖", "📚", "🎓"], "exam": ["📝", "📚"],
    "car": ["🚗", "🚙"], "travel": ["✈️", "🧳", "🌍"], "plane": ["✈️", "🛫"],
    "home": ["🏠", "🏡"], "house": ["🏠", "🏡"], "doctor": ["🩺", "🏥"],
    "sick": ["🤒", "🤕", "💊"], "medicine": ["💊", "💉"], "friend": ["🤝", "❤️"],
    "family": ["👨‍👩‍👧‍👦", "❤️"], "cat": ["🐱", "🐈"], "dog": ["🐶", "🐕"],
    "bird": ["🐦", "🕊️"], "fish": ["🐟", "🐠"], "tree": ["🌳", "🌲"],
    "today": ["📅"], "tomorrow": ["📆"], "baby": ["👶", "🍼"], "cool": ["😎", "🆒"],
    "strong": ["💪"], "pray": ["🤲", "🕌"], "sorry": ["🙏", "😔"], "please": ["🙏"],
    "miss": ["🥺", "💔"], "surprise": ["😲", "🤯"], "tired": ["😴", "🥱"],
}

# ---------------------------------------------------------------------------
# تولید کاتلین
# ---------------------------------------------------------------------------
def dedupe(seq):
    seen, out = set(), []
    for s in seq:
        s = s.rstrip("?؟")
        if s and s not in seen:
            seen.add(s)
            out.append(s)
    return out

FA = dedupe(FA)
COLLOQ = dedupe(COLLOQ)
EN = dedupe(EN)

def word_list(words, start=2000, step=2, floor=60):
    lines = []
    for i, w in enumerate(words):
        f = max(start - i * step, floor)
        lines.append(f'        "{k(w)}" to {f},')
    return "\n".join(lines)

def colloq_list(words):
    lines = []
    for i, w in enumerate(words):
        f = max(500 - i * 2, 80)
        lines.append(f'        "{k(w)}" to {f},')
    return "\n".join(lines)

def bigram_list(triples):
    lines = []
    for (a, b, c) in triples:
        lines.append(f'        Triple("{k(a)}", "{k(b)}", {c}),')
    return "\n".join(lines)

def emoji_list(d):
    lines = []
    for key, vals in d.items():
        joined = ", ".join(f'"{v}"' for v in vals)
        lines.append(f'        "{k(key)}" to listOf({joined}),')
    return "\n".join(lines)

kt = f'''package com.niktype.prediction.data

/**
 * داده‌های واژگانی NikType — کاملاً محلی و بدون شبکه.
 * بسامدها رتبه‌بندی تقریبی هستند و هدفشان مرتب‌سازی پیشنهادهاست.
 * واژه‌نامه کاربر به‌مرور در همین ساختارها (روی دستگاه) غنی می‌شود.
 *
 * تولیدشده توسط scripts/gen_wordlists.py
 */
object WordData {{

    /** واژگان پایه فارسی */
    val PERSIAN_WORDS: List<Pair<String, Int>> = listOf(
{word_list(FA)}
    )

    /** واژگان محاوره‌ای فارسی */
    val COLLOQUIAL_FA: List<Pair<String, Int>> = listOf(
{colloq_list(COLLOQ)}
    )

    /** واژگان پایه انگلیسی */
    val ENGLISH_WORDS: List<Pair<String, Int>> = listOf(
{word_list(EN)}
    )

    /** بی‌گرام‌های بذر فارسی */
    val BIGRAMS_FA: List<Triple<String, String, Int>> = listOf(
{bigram_list(BIGRAMS_FA)}
    )

    /** بی‌گرام‌های بذر انگلیسی */
    val BIGRAMS_EN: List<Triple<String, String, Int>> = listOf(
{bigram_list(BIGRAMS_EN)}
    )

    /** نگاشت کلیدواژه به ایموجی */
    val EMOJI_KEYWORDS: Map<String, List<String>> = mapOf(
{emoji_list(EMOJI)}
    )
}}
'''

os.makedirs(os.path.dirname(OUT), exist_ok=True)
with io.open(OUT, "w", encoding="utf-8") as f:
    f.write(kt)

print("written:", OUT)
print("fa:", len(FA), "colloq:", len(COLLOQ), "en:", len(EN), "bigrams_fa:", len(BIGRAMS_FA), "bigrams_en:", len(BIGRAMS_EN), "emoji:", len(EMOJI))
