# یادکار (PersianTodo)

اپلیکیشن اندروید مدیریت وظایف با رابط کاربری فارسی (راست‌به‌چپ) و تقویم هجری شمسی، در قالبی مشابه Microsoft To Do.

## ویژگی‌ها
- فهرست‌های هوشمند: روز من، مهم، برنامه‌ریزی‌شده، وظایف، انجام‌شده
- فهرست‌های سفارشی (مثل «خرید»، «کار»)
- انتخاب تاریخ **و ساعت** سررسید با **تقویم شمسی** اختصاصی (بدون نیاز به کتابخانهٔ خارجی)
- **یادآوری با اعلان سیستم** در زمان مشخص‌شده (AlarmManager + Notification)
- **تب تقویم** به سبک Google Calendar: نمای امروز، ۷ روز آینده، و ماه — با نمایش وظایف به‌صورت جعبهٔ کوچک در هر روز
- **ارسال خودکار پیامک**: انتخاب مخاطب یا وارد کردن شماره + متن دلخواه، ارسال در زمان تعیین‌شده
- **تماس تلفنی خودکار**: در زمان تعیین‌شده با شماره موردنظر تماس گرفته می‌شود و متن شما به‌عنوان یادآوری روی صفحه نمایش داده می‌شود (توضیح محدودیت در پایین)
- طراحی الهام‌گرفته از iOS: دکمه‌های کپسولی، رنگ آبی iOS، تب‌بار سگمنتی
- ذخیره‌سازی محلی با Room (بدون نیاز به اینترنت)
- ساخته‌شده با Kotlin + Jetpack Compose + Material 3

### ⚠️ دربارهٔ ویژگی تماس (IVR)
اندروید به برنامه‌های عادی اجازه نمی‌دهد صدای تولیدشده را به‌صورت خودکار داخل یک تماس واقعی پخش کنند —
این محدودیت عمدی پلتفرم برای جلوگیری از سوءاستفادهٔ رباکال است. آنچه این نسخه انجام می‌دهد: به‌صورت
خودکار با شماره تماس می‌گیرد و متن شما را به‌عنوان اعلان روی صفحه نشان می‌دهد تا خودتان بخوانید.
برای تماس خودکار واقعی با گفتار مصنوعی برای طرف مقابل (IVR واقعی)، باید از یک سرویس تماس ابری مثل
Twilio Voice استفاده شود که نیاز به حساب کاربری و پرداخت هزینه به ازای هر تماس دارد — در صورت تمایل
می‌توان این یکپارچه‌سازی را در مرحلهٔ بعد اضافه کرد.

### مجوزهای موردنیاز
برنامه هنگام روشن‌کردن هر ویژگی، مجوز مربوطه را در لحظه درخواست می‌کند:
- اعلان‌ها (Android 13+)
- ارسال پیامک (SEND_SMS)
- تماس تلفنی (CALL_PHONE)
- زمان‌بندی دقیق هشدار (در صورت نیاز، در برخی گوشی‌ها از تنظیمات سیستم)

## ساختار پروژه
```
app/src/main/java/ir/moeini/persiantodo/
├── MainActivity.kt
├── data/            # Entities, DAOs, Room database, Repository
├── viewmodel/        # TaskViewModel
├── util/            # PersianCalendar.kt — تبدیل تاریخ میلادی/شمسی
└── ui/
    ├── theme/       # رنگ، تایپوگرافی، تم Compose
    ├── screens/     # تقویم شمسی، فرم افزودن/ویرایش وظیفه، ردیف وظیفه
    └── PersianTodoApp.kt  # ناوبری و صفحهٔ اصلی
```

## اجرا
1. پروژه را در **Android Studio** (نسخهٔ Koala یا جدیدتر) باز کنید.
2. صبر کنید تا Gradle وابستگی‌ها را دانلود کند.
3. روی یک دستگاه یا شبیه‌ساز اندروید (نسخهٔ ۷ / API 24 به بالا) اجرا کنید.

## قرار دادن روی گیت‌هاب
```bash
git init
git add .
git commit -m "Initial commit: Persian todo app with Jalali calendar"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

## گام‌های بعدی پیشنهادی
- افزودن فونت فارسی اختصاصی (مثل Vazirmatn) در `res/font`
- یادآوری‌ها (WorkManager + AlarmManager) برای وظایف دارای سررسید
- همگام‌سازی ابری (Firebase یا سرور اختصاصی)
- ویجت صفحهٔ اصلی برای «روز من»
- پشتیبانی از تکرار وظیفه (فیلد `repeatRule` در Entity از قبل آماده شده است)

---

## English summary
Kotlin + Jetpack Compose Android todo app, Microsoft To Do–style, with a fully
custom Jalali/Shamsi (Hijri Shamsi) calendar converter (`util/PersianCalendar.kt`,
pure math, no external dependency) and a Persian RTL UI throughout. Local
persistence via Room. Open in Android Studio and run — see build instructions
above. No GitHub remote is configured; push it to your own repo with the
commands above once you're ready.
