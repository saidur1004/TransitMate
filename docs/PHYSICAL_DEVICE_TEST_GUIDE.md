# TransitMate — বাস্তব Android ফোনে NFC ও Rapid Pass পরীক্ষার সহজ নির্দেশিকা

**অ্যাপ নাম:** TransitMate (`com.transitmate.bd.debug`)  
**লক্ষ্য:** Phase 09.1 — ফিক্স-পরবর্তী ফিজিক্যাল **Rapid Pass** যাচাইকরণ (NFC Detection, Card Classification, Balance, History, Repeated Scan ও Persistence পরীক্ষা)

> ⚠️ **গুরুত্বপূর্ণ সততা ও গোপনীয়তা নিয়ম:**
> 1. কোনো কার্ডের ব্যালেন্স বা ভ্রমণ তথ্য পড়তে না পারলে (বা MRT Pass অপ্রাপ্ত/সুরক্ষিত হলে) অ্যাপ অনুমাননির্ভর সংখ্যা দেখাবে না—এবং রিপোর্টে তা **`Not Available` / `Unverified`** হিসেবে সত্যভাবে জানাতে হবে।
> 2. **IDm গোপনীয়তা:** স্ক্রিনশট, স্ক্রিন-রেকর্ডিং, লগ বা GitHub রিপোর্টে কখনোই আপনার কার্ডের সম্পূর্ণ **8-byte IDm** প্রকাশ করবেন না। শেয়ার করার আগে মাঝের ৫টি বাইট অবশ্যই ঢেকে দেবেন (যেমন: `01:27:**:**:**:**:**:A0`)।

---

## ১. পরীক্ষা শুরুর আগে (Before Testing)

1. **NFC-সমর্থিত Android ফোন নিশ্চিত করুন:**
   - ফোনের পেছনে বিল্ট-ইন NFC হার্ডওয়্যার থাকতে হবে (Android 8.0 / API 26 বা তার উপরের সংস্করণ)।
2. **Android Settings থেকে NFC চালু করুন:**
   - ফোনের **Settings → Connected devices → Connection preferences → NFC** (অথবা Quick Settings টাইল) অন করুন।
   - কার্ড সহজে পাওয়ার জন্য ফোনের মোটা ধাতব (metal) বা ম্যাগনেটিক ব্যাক কভার থাকলে খুলে নিন।
3. **যাচাইকৃত Debug APK ইনস্টল করুন:**
   - কম্পিউটার থেকে ফোনে [`app/build/outputs/apk/debug/app-debug.apk`](../app/build/outputs/apk/debug/app-debug.apk) কপি করে ফাইল ম্যানেজার থেকে ইনস্টল করুন (প্রয়োজনে *Install unknown apps* অনুমতি দিন), অথবা ADB দিয়ে চালান:
     ```bash
     adb install -r app/build/outputs/apk/debug/app-debug.apk
     ```
4. **TransitMate ওপেন করে NFC Diagnostics দেখে নিন:**
   - অ্যাপ ওপেন করে Onboarding (`Get Started` / `Skip`) সম্পন্ন করুন।
   - **Settings → NFC Diagnostics** স্ক্রিনে গিয়ে `NFC Hardware` অংশে হার্ডওয়্যার প্রস্তুত (Ready) দেখাচ্ছে কি না এবং Reader Flags (`NFC_F | NFC_A | SKIP_NDEF`) দেখাচ্ছে কি না যাচাই করুন। *(এই পর্যায়ে **Home** স্ক্রিনেও কোনো "No NFC" বা "NFC Disabled" সতর্কতা থাকবে না।)*
5. **ফিজিক্যাল Rapid Pass কার্ড হাতে প্রস্তুত রাখুন।** *(যদি আপনার কাছে আসল **MRT Pass** কার্ড না থাকে, তবে MRT অংশ `Blocked` হিসেবেই থাকবে।)*

---

## ২. পরীক্ষা চলাকালীন ধাপসমূহ (During Testing)

প্রতিটি ধাপ ধীরে ধীরে সম্পন্ন করুন এবং প্রতিটি স্ক্যান শেষ না হওয়া পর্যন্ত কার্ড সরাবেন না:

1. **প্রথম স্ক্যান (First Scan):**
   - নিচের নেভিগেশন বার থেকে **Scan** ট্যাবে যান।
   - Rapid Pass কার্ডটি ফোনের পেছনের উপরের-মাঝখানের NFC অ্যান্টেনার ওপর **১.৫ থেকে ২.৫ সেকেন্ড** স্থিরভাবে ধরে রাখুন।
   - **Scan Result** স্ক্রিন না খোলা পর্যন্ত অপেক্ষা করুন।
2. **Card Type, Balance ও Transaction History যাচাই করুন:**
   - **Card Title & Issuer:** কার্ডটি **`Rapid Pass`** (`DTCA · FeliCa (NFC-F)`) হিসেবে সঠিকভাবে শনাক্ত হয়েছে কি না দেখুন (মেট্রো কাউন্টারে রিচার্জ করা থাকলেও এটি যেন `MRT Pass` হিসেবে ভুল না দেখায়)।
   - **Card IDm:** `IDm` সঠিকভাবে দেখাচ্ছে কি না এবং **Copy IDm** বাটন কাজ করে কি না পরীক্ষা করুন।
   - **Balance:** `LATEST VERIFIED BALANCE`-এ দেখানো টাকা (`৳ ...`) আপনার প্রকৃত কার্ড ব্যালেন্সের সাথে মেলে কি না যাচাই করুন।
   - **Transaction History:** ভ্রমণ (`- ৳`) ও টপ-আপ (`+ ৳`) রেকর্ড, স্টেশনের নাম এবং সময় (`English` ও `বাংলা` উভয় ভাষায়) সঠিকভাবে দেখাচ্ছে কি না দেখুন।
3. **কার্ড সেভ ও দ্বিতীয়বার স্ক্যান (Repeated Scan Check):**
   - **Scan Result** থেকে **+ Save to My Cards** চেপে কার্ডটি সংরক্ষণ করুন।
   - **History** / **Insights** স্ক্রিনে মোট কয়টি ট্রানজ্যাকশন রেকর্ড সংরক্ষিত হলো তা লিখে রাখুন (ধরা যাক `N`টি)।
   - এরপর **Tap Card to Refresh** অথবা **Scan** ট্যাব থেকে **একই Rapid Pass কার্ডটি দ্বিতীয়বার স্ক্যান** করুন এবং স্ক্যান শেষ হওয়ার জন্য অপেক্ষা করুন।
4. **ডুপ্লিকেট বা পুরোনো তথ্য মুছে যাওয়া পরীক্ষা (Deduplication & Preservation):**
   - দ্বিতীয় স্ক্যানের পর **My Cards**–এ একই কার্ড দুইবার তৈরি হয়েছে কি না দেখুন (মাত্র **১টি** কার্ড থাকার কথা)।
   - **Transaction History** এবং **Insights**–এ গিয়ে দেখুন একই ট্রানজ্যাকশন ডুপ্লিকেট হয়েছে কি না (`N`টি থাকার কথা, বা নতুন ভ্রমণ যোগ হলে `N + k`টি) এবং **আগের সংরক্ষিত কোনো ট্রানজ্যাকশন মুছে গেছে কি না**।
5. **অ্যাপ রিস্টার্ট ও অফলাইন স্থায়িত্ব পরীক্ষা (Restart & Persistence Check):**
   - ফোনে ইন্টারনেট/মোবাইল ডেটা বন্ধ রাখুন (বা **Airplane Mode ON** করুন)।
   - Recent Apps থেকে TransitMate পুরোপুরি বন্ধ করে (**Force Stop / Swipe Close**) আবার চালু করুন।
   - চেক করুন আপনার সেভ করা কার্ড, ব্যালেন্স, ট্রানজ্যাকশন ইতিহাস, থিম ও ভাষা ঠিকঠাক থেকে গেছে কি না এবং **Settings → Data Management → Export History as CSV (UTF-8)** কাজ করে কি না।
6. **ত্রুটি সামলানোর পরীক্ষা (Error Handling Check):**
   - **Scan** স্ক্রিনে কার্ড ছুঁইয়েই `০.২` সেকেন্ডের মধ্যে সরিয়ে ফেলুন — অ্যাপ ক্র্যাশ না করে **"Card moved too early / Hold card steady"** ধরনের বার্তা ও **Try Again** অপশন দিচ্ছে কি না দেখুন।
   - সম্ভব হলে একটি সাধারণ ব্যাংক NFC কার্ড (যেমন Visa/Mastercard contactless) ধরে দেখুন — অ্যাপ **Unsupported Card** দেখিয়ে নিরাপদে সামলে নিচ্ছে কি না পরীক্ষা করুন।

---

## ৩. প্রমাণ সংগ্রহ ফরম্যাট (Evidence Collection)

পরীক্ষা শেষে নিচের তথ্যগুলো নোট করে বা পূরণ করে [`docs/PHYSICAL_DEVICE_TEST_CHECKLIST.md`](./PHYSICAL_DEVICE_TEST_CHECKLIST.md)–এর সাথে শেয়ার করুন:

| তথ্য (Field) | কী লিখবেন / সংযুক্ত করবেন |
|---|---|
| **Phone Manufacturer & Model** | যেমন: `Samsung Galaxy S23` / `Google Pixel 7` / `Xiaomi 13` |
| **Android Version** | যেমন: `Android 14` / `Android 15` |
| **TransitMate Version** | `v1.0.0` (`versionCode = 1`, Debug APK) |
| **Card Type** | `Rapid Pass (DTCA)` *(বা MRT Pass পেলে তা উল্লেখ করুন)* |
| **Test ID (`RP-01` – `RP-10`)** | কোন কোন টেস্ট কেস চালিয়েছেন (`RP-01` থেকে `RP-10`) |
| **Expected Result vs Actual Result** | বাস্তবে স্ক্রিনে কী দেখা গেছে (বিস্তারিত) |
| **Status** | `Pass` / `Fail` / `Blocked` |
| **Screenshot / Error Log** | স্ক্রিনশট (⚠️ **মাঝের IDm বাইট ঢেকে**) এবং কোনো সমস্যা হলে `adb logcat` আউটপুট |

> 📋 বিস্তারিত প্রতিটি টেস্ট কেসের স্টেপ-বাই-স্টেপ তালিকা [`docs/PHYSICAL_DEVICE_TEST_CHECKLIST.md`](./PHYSICAL_DEVICE_TEST_CHECKLIST.md) ফাইলে রয়েছে।
