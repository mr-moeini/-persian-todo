# یادکار (PersianTodo)

اپلیکیشن اندروید مدیریت وظایف با رابط کاربری فارسی (راست‌به‌چپ) و تقویم هجری شمسی، در قالبی مشابه Microsoft To Do.

## ویژگی‌ها
- فهرست‌های هوشمند: روز من، مهم، برنامه‌ریزی‌شده، وظایف، انجام‌شده
- فهرست‌های سفارشی (مثل «خرید»، «کار»)
- انتخاب تاریخ سررسید با **تقویم شمسی** اختصاصی (بدون نیاز به کتابخانهٔ خارجی)
- ذخیره‌سازی محلی با Room (بدون نیاز به اینترنت)
- ساخته‌شده با Kotlin + Jetpack Compose + Material 3

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
