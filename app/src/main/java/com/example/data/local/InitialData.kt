package com.example.data.local

import com.example.data.local.entity.*

object InitialData {

    val initialQuotes = listOf(
        // --- AYET-İ KERİMELER ---
        QuoteEntity(
            text = "Şüphesiz güçlükle beraber bir kolaylık vardır. Gerçekten güçlükle beraber bir kolaylık vardır.",
            source = "İnşirah Sûresi, 5-6. Ayet",
            category = QuoteCategory.AYET,
            isFavorite = true
        ),
        QuoteEntity(
            text = "De ki: Rabbim, ilmimi artır!",
            source = "Tâhâ Sûresi, 114. Ayet",
            category = QuoteCategory.AYET,
            isFavorite = true
        ),
        QuoteEntity(
            text = "İnsan için ancak çalıştığının karşılığı vardır ve şüphesiz onun çalışması ileride görülecektir.",
            source = "Necm Sûresi, 39-40. Ayet",
            category = QuoteCategory.AYET
        ),
        QuoteEntity(
            text = "Bana dua edin, size cevap vereyim (duanızı kabul edeyim).",
            source = "Mü'min Sûresi, 60. Ayet",
            category = QuoteCategory.AYET
        ),
        QuoteEntity(
            text = "Öyleyse yalnız beni anın ki ben de sizi anayım. Bana şükredin ve nankörlük etmeyin.",
            source = "Bakara Sûresi, 152. Ayet",
            category = QuoteCategory.AYET
        ),
        QuoteEntity(
            text = "Kim Allah'a tevekkül ederse, O kendisine yeter. Şüphesiz Allah emrini yerine getirendir.",
            source = "Talâk Sûresi, 3. Ayet",
            category = QuoteCategory.AYET
        ),
        QuoteEntity(
            text = "Allah hiç kimseye güç yetireceğinden fazlasını yüklemez.",
            source = "Bakara Sûresi, 286. Ayet",
            category = QuoteCategory.AYET
        ),
        QuoteEntity(
            text = "De ki: Hiç bilenlerle bilmeyenler bir olur mu? Ancak akıl sahipleri öğüt alır.",
            source = "Zümer Sûresi, 9. Ayet",
            category = QuoteCategory.AYET
        ),
        QuoteEntity(
            text = "Sabret; çünkü Allah iyilik edenlerin mükâfatını asla zayi etmez.",
            source = "Hûd Sûresi, 115. Ayet",
            category = QuoteCategory.AYET
        ),
        QuoteEntity(
            text = "Biliniz ki kalpler ancak Allah'ı anmakla huzur ve sükûna kavuşur.",
            source = "Ra'd Sûresi, 28. Ayet",
            category = QuoteCategory.AYET
        ),

        // --- HADİS-İ ŞERİFLER ---
        QuoteEntity(
            text = "İlim öğrenmek, kadın-erkek her Müslümana farzdır.",
            source = "İbn Mâce, Mukaddime 17",
            category = QuoteCategory.HADIS,
            isFavorite = true
        ),
        QuoteEntity(
            text = "Kolaylaştırınız, zorlaştırmayınız; müjdeleyiniz, nefret ettirmeyiniz.",
            source = "Buhârî, İlim 11",
            category = QuoteCategory.HADIS,
            isFavorite = true
        ),
        QuoteEntity(
            text = "İki nimet vardır ki insanların çoğu bunların kıymetini bilmekte aldanmıştır: Sağlık ve boş vakit.",
            source = "Buhârî, Rikâk 1",
            category = QuoteCategory.HADIS
        ),
        QuoteEntity(
            text = "İnsanların en hayırlısı, insanlara en çok faydası dokunandır.",
            source = "Beyhakî, Şuabü'l-Îmân",
            category = QuoteCategory.HADIS
        ),
        QuoteEntity(
            text = "Kim ilim tahsil etmek için bir yola çıkarsa, Allah ona cennete giden yolu kolaylaştırır.",
            source = "Müslim, Zikir 38",
            category = QuoteCategory.HADIS
        ),
        QuoteEntity(
            text = "Ameller ancak niyetlere göredir; herkese ancak niyet ettiği şey vardır.",
            source = "Buhârî, Bed'ü'l-Vahy 1",
            category = QuoteCategory.HADIS
        ),
        QuoteEntity(
            text = "Hiç kimse kendi el emeğinden daha hayırlı bir rızık yememiştir.",
            source = "Buhârî, Büyû' 15",
            category = QuoteCategory.HADIS
        ),
        QuoteEntity(
            text = "Hayra vesile olan, o hayrı bizzat işlemiş gibidir.",
            source = "Tirmizî, İlim 14",
            category = QuoteCategory.HADIS
        ),
        QuoteEntity(
            text = "Güçlü mümin, zayıf müminden daha hayırlı ve Allah katında daha sevimlidir.",
            source = "Müslim, Kader 34",
            category = QuoteCategory.HADIS
        ),
        QuoteEntity(
            text = "Tebessüm etmek sadakadır.",
            source = "Tirmizî, Birr 36",
            category = QuoteCategory.HADIS
        ),

        // --- HİKMETLİ SÖZLER & ALİMLER ---
        QuoteEntity(
            text = "Bana bir harf öğretenin kırk yıl kölesi olurum.",
            source = "Hz. Ali (r.a.)",
            category = QuoteCategory.HIKMET,
            isFavorite = true
        ),
        QuoteEntity(
            text = "Dün akıllıydım, dünyayı değiştirmek istedim. Bugün bilgeyim, kendimi değiştiriyorum.",
            source = "Mevlana Celaleddin Rumi",
            category = QuoteCategory.HIKMET
        ),
        QuoteEntity(
            text = "İlim ilim bilmektir, ilim kendin bilmektir. Sen kendini bilmezsin, ya nice okumaktır.",
            source = "Yunus Emre",
            category = QuoteCategory.HIKMET
        ),
        QuoteEntity(
            text = "Akıl gibi mal, iyi huy gibi dost, edep gibi miras, ilim gibi şeref yoktur.",
            source = "Hz. Ali (r.a.)",
            category = QuoteCategory.HIKMET
        ),
        QuoteEntity(
            text = "İlim ve sanat takdir edilmediği yerden göç eder.",
            source = "İbn Sînâ",
            category = QuoteCategory.HIKMET
        ),
        QuoteEntity(
            text = "İmkânın sınırını görmek için imkânsızı denemek gerekir.",
            source = "Fatih Sultan Mehmet",
            category = QuoteCategory.HIKMET
        ),
        QuoteEntity(
            text = "Sabır acıdır; lakin meyvesi çok tatlıdır.",
            source = "Sadi Şirazi",
            category = QuoteCategory.HIKMET
        ),
        QuoteEntity(
            text = "Gençliğini ilimle süslemeyen, ihtiyarlığında cehaletin yükünü taşımak zorunda kalır.",
            source = "İbn Haldun",
            category = QuoteCategory.HIKMET
        ),
        QuoteEntity(
            text = "Kalp temizliği olmadan ilim, ruha ağır bir yükten ibarettir.",
            source = "İmam Gazâlî",
            category = QuoteCategory.HIKMET
        )
    )

    fun getSampleHomeworks(): List<HomeworkEntity> {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000
        return listOf(
            HomeworkEntity(
                title = "Türev ve İntegral 40 Soru Testi",
                course = "Matematik",
                type = HomeworkType.NORMAL,
                description = "Sayfa 142-148 arasındaki tüm sorular çözülüp deftere yazılacak.",
                dueDateMillis = now + 2 * oneDay,
                priority = Priority.YUKSEK
            ),
            HomeworkEntity(
                title = "Milli Edebiyat Dönemi Tiyatro İncelemesi",
                course = "Edebiyat",
                type = HomeworkType.PERFORMANS,
                description = "Reşat Nuri Güntekin veya Faruk Nafiz Çamlıbel'in bir eseri seçilip tahlil edilecek.",
                dueDateMillis = now + 5 * oneDay,
                priority = Priority.ORTA
            ),
            HomeworkEntity(
                title = "Alternatif Enerji Kaynakları Modeli",
                course = "Fizik",
                type = HomeworkType.PROJE,
                description = "Güneş veya rüzgar enerjisi çalışma prensibini gösteren maket ve rapor teslimi.",
                dueDateMillis = now + 12 * oneDay,
                priority = Priority.YUKSEK
            )
        )
    }

    fun getSampleExams(): List<ExamEntity> {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000
        return listOf(
            ExamEntity(
                courseName = "Matematik",
                examTitle = "1. Dönem 2. Yazılı Sınavı",
                examDateMillis = now + 4 * oneDay + 3 * 3600 * 1000,
                topics = "Trigonometri, Polinomlar, İkinci Dereceden Denklemler",
                targetGrade = 95
            ),
            ExamEntity(
                courseName = "Fizik",
                examTitle = "Ortak Yazılı Sınav",
                examDateMillis = now + 7 * oneDay + 2 * 3600 * 1000,
                topics = "Elektrik, Manyetizma ve Dalgalar",
                targetGrade = 90
            ),
            ExamEntity(
                courseName = "Biyoloji",
                examTitle = "Genetik ve Hücre Bölünmesi",
                examDateMillis = now + 11 * oneDay + 4 * 3600 * 1000,
                topics = "Mitoz, Mayoz ve Kalıtım İlkeleri",
                targetGrade = 100
            )
        )
    }

    fun getSampleSchedule(): List<ScheduleLessonEntity> {
        return listOf(
            // Pazartesi (1)
            ScheduleLessonEntity(dayOfWeek = 1, periodIndex = 1, lessonName = "Matematik", teacherOrRoom = "Oda 201 - Selim Hoca", startTime = "08:30", endTime = "09:10", colorHex = "#0F5A4F"),
            ScheduleLessonEntity(dayOfWeek = 1, periodIndex = 2, lessonName = "Matematik", teacherOrRoom = "Oda 201 - Selim Hoca", startTime = "09:20", endTime = "10:00", colorHex = "#0F5A4F"),
            ScheduleLessonEntity(dayOfWeek = 1, periodIndex = 3, lessonName = "Fizik", teacherOrRoom = "Laboratuvar - Zeynep Hoca", startTime = "10:15", endTime = "10:55", colorHex = "#2C568E"),
            ScheduleLessonEntity(dayOfWeek = 1, periodIndex = 4, lessonName = "Fizik", teacherOrRoom = "Laboratuvar - Zeynep Hoca", startTime = "11:05", endTime = "11:45", colorHex = "#2C568E"),
            ScheduleLessonEntity(dayOfWeek = 1, periodIndex = 5, lessonName = "Edebiyat", teacherOrRoom = "Oda 201 - Mehmet Hoca", startTime = "12:45", endTime = "13:25", colorHex = "#915D00"),
            ScheduleLessonEntity(dayOfWeek = 1, periodIndex = 6, lessonName = "Tarih", teacherOrRoom = "Oda 201 - Ayşe Hoca", startTime = "13:35", endTime = "14:15", colorHex = "#7C3AED"),

            // Salı (2)
            ScheduleLessonEntity(dayOfWeek = 2, periodIndex = 1, lessonName = "Kimya", teacherOrRoom = "Lab 2 - Ömer Hoca", startTime = "08:30", endTime = "09:10", colorHex = "#0284C7"),
            ScheduleLessonEntity(dayOfWeek = 2, periodIndex = 2, lessonName = "Kimya", teacherOrRoom = "Lab 2 - Ömer Hoca", startTime = "09:20", endTime = "10:00", colorHex = "#0284C7"),
            ScheduleLessonEntity(dayOfWeek = 2, periodIndex = 3, lessonName = "Biyoloji", teacherOrRoom = "Oda 105 - Fatma Hoca", startTime = "10:15", endTime = "10:55", colorHex = "#16A34A"),
            ScheduleLessonEntity(dayOfWeek = 2, periodIndex = 4, lessonName = "Biyoloji", teacherOrRoom = "Oda 105 - Fatma Hoca", startTime = "11:05", endTime = "11:45", colorHex = "#16A34A"),
            ScheduleLessonEntity(dayOfWeek = 2, periodIndex = 5, lessonName = "İngilizce", teacherOrRoom = "Dil Sınıfı - John Hoca", startTime = "12:45", endTime = "13:25", colorHex = "#D97706"),
            ScheduleLessonEntity(dayOfWeek = 2, periodIndex = 6, lessonName = "Din Kültürü", teacherOrRoom = "Oda 201 - Halil Hoca", startTime = "13:35", endTime = "14:15", colorHex = "#0D7C66"),

            // Çarşamba (3)
            ScheduleLessonEntity(dayOfWeek = 3, periodIndex = 1, lessonName = "Geometri", teacherOrRoom = "Oda 201 - Selim Hoca", startTime = "08:30", endTime = "09:10", colorHex = "#0F5A4F"),
            ScheduleLessonEntity(dayOfWeek = 3, periodIndex = 2, lessonName = "Geometri", teacherOrRoom = "Oda 201 - Selim Hoca", startTime = "09:20", endTime = "10:00", colorHex = "#0F5A4F"),
            ScheduleLessonEntity(dayOfWeek = 3, periodIndex = 3, lessonName = "Edebiyat", teacherOrRoom = "Oda 201 - Mehmet Hoca", startTime = "10:15", endTime = "10:55", colorHex = "#915D00"),
            ScheduleLessonEntity(dayOfWeek = 3, periodIndex = 4, lessonName = "Felsefe", teacherOrRoom = "Oda 201 - Canan Hoca", startTime = "11:05", endTime = "11:45", colorHex = "#64748B"),

            // Perşembe (4)
            ScheduleLessonEntity(dayOfWeek = 4, periodIndex = 1, lessonName = "Matematik", teacherOrRoom = "Oda 201 - Selim Hoca", startTime = "08:30", endTime = "09:10", colorHex = "#0F5A4F"),
            ScheduleLessonEntity(dayOfWeek = 4, periodIndex = 2, lessonName = "Fizik", teacherOrRoom = "Oda 201 - Zeynep Hoca", startTime = "09:20", endTime = "10:00", colorHex = "#2C568E"),
            ScheduleLessonEntity(dayOfWeek = 4, periodIndex = 3, lessonName = "Coğrafya", teacherOrRoom = "Oda 104 - Murat Hoca", startTime = "10:15", endTime = "10:55", colorHex = "#EA580C"),
            ScheduleLessonEntity(dayOfWeek = 4, periodIndex = 4, lessonName = "İngilizce", teacherOrRoom = "Dil Sınıfı - John Hoca", startTime = "11:05", endTime = "11:45", colorHex = "#D97706"),

            // Cuma (5)
            ScheduleLessonEntity(dayOfWeek = 5, periodIndex = 1, lessonName = "Kimya", teacherOrRoom = "Lab 2 - Ömer Hoca", startTime = "08:30", endTime = "09:10", colorHex = "#0284C7"),
            ScheduleLessonEntity(dayOfWeek = 5, periodIndex = 2, lessonName = "Biyoloji", teacherOrRoom = "Oda 105 - Fatma Hoca", startTime = "09:20", endTime = "10:00", colorHex = "#16A34A"),
            ScheduleLessonEntity(dayOfWeek = 5, periodIndex = 3, lessonName = "Edebiyat", teacherOrRoom = "Oda 201 - Mehmet Hoca", startTime = "10:15", endTime = "10:55", colorHex = "#915D00"),
            ScheduleLessonEntity(dayOfWeek = 5, periodIndex = 4, lessonName = "Beden Eğitimi", teacherOrRoom = "Spor Salonu", startTime = "11:05", endTime = "11:45", colorHex = "#059669")
        )
    }

    fun getSampleGoals(dateStr: String): List<DailyGoalEntity> {
        return listOf(
            DailyGoalEntity(
                title = "60 Matematik Sorusu Çöz",
                category = GoalCategory.AKADEMIK,
                targetCount = 60,
                currentCount = 45,
                unit = "soru",
                isCompleted = false,
                dateString = dateStr
            ),
            DailyGoalEntity(
                title = "Kuran-ı Kerim Meal Oku (1 Cüz / Sure)",
                category = GoalCategory.MANEVIYAT,
                targetCount = 10,
                currentCount = 10,
                unit = "sayfa",
                isCompleted = true,
                dateString = dateStr
            ),
            DailyGoalEntity(
                title = "Günün Hadis ve Hikmetli Sözünü Tefekkür Et",
                category = GoalCategory.MANEVIYAT,
                targetCount = 1,
                currentCount = 1,
                unit = "kez",
                isCompleted = true,
                dateString = dateStr
            ),
            DailyGoalEntity(
                title = "Fizik Ders Notlarını Tekrar Et",
                category = GoalCategory.AKADEMIK,
                targetCount = 30,
                currentCount = 0,
                unit = "dk",
                isCompleted = false,
                dateString = dateStr
            ),
            DailyGoalEntity(
                title = "Kitap Okuma (İlmi / Edebi Eser)",
                category = GoalCategory.KISISISIL_GELISIM,
                targetCount = 25,
                currentCount = 20,
                unit = "sayfa",
                isCompleted = false,
                dateString = dateStr
            )
        )
    }
}
