# PARS CHAT

اپلیکیشن اندرویدی Kotlin و Jetpack Compose با رابط فارسی و الهام‌گرفته از هنر ایران باستان.

- سازنده: بنیامین نوری دفرازی
- Android applicationId: `com.bentech.parschaatt`
- Kotlin namespace: `com.bentech.parschat`
- حداقل نسخه: Android 7 / API 24
- Target SDK: 34
- وضعیت فعلی: نمونه اولیه قابل نصب؛ پیام‌رسانی آنلاین هنوز پیاده‌سازی و تأیید نشده است.

## قابلیت‌های موجود در سورس فعلی
- صفحه آغازین و فهرست ۸ گفتگوی نمونه
- رابط چت با پیام‌های محلیِ حافظه‌ای
- نمایه و تنظیم حالت تاریک
- صفحه تماس نمایشی (بدون اتصال WebRTC)
- وابستگی‌های Firebase Auth و Cloud Firestore در Gradle
- GitHub Actions برای ساخت APK دیباگ با پیکربندی Firebase

## دریافت APK
از تب Actions آخرین اجرای موفق را باز کنید و Artifact با نام `PARS-CHAT-debug-apk` را دریافت کنید.

## وضعیت Firebase
وجود `google-services.json` در فرایند Build به معنی فعال بودن سرویس‌های Firebase یا پیاده‌سازی ورود و پیام‌رسانی نیست. برای نسخه آنلاین باید Authentication و Firestore در پروژه Firebase فعال شوند و منطق کلاینت، قوانین امنیتی و تست‌های دسترسی تکمیل شوند.

فایل تنظیمات Firebase و کلیدهای خصوصی را در مخزن عمومی Commit نکنید.
