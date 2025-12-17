# 📱 Cập nhật Profile & Category Management Layout

## 🎯 Mục tiêu
1. Cập nhật layout trang Profile (Tài khoản & Hồ sơ) theo thiết kế mới
2. Cập nhật trang Quản lý Danh mục để hiển thị theo section (Chi tiêu & Thu nhập)

---

## ✅ Những gì đã thực hiện

### 1. Activity Profile (activity_profile.xml)

#### Thay đổi:
- ✅ **Đơn giản hóa header**: Bỏ button search/more, chỉ giữ lại nút Back và Title
- ✅ **Giữ nguyên thông tin người dùng**: Avatar, tên, email trong card màu xanh
- ✅ **Giữ nguyên menu items**:
  - Đơn vị tiền tệ
  - Quản lý danh mục  
  - Đăng xuất
  - Xóa tài khoản

#### Layout Structure:
```
CoordinatorLayout
├── LinearLayout (Main Container)
│   ├── Header (Back + Title)
│   ├── ScrollView
│   │   ├── User Info Card (Màu xanh primary)
│   │   │   ├── Avatar Icon
│   │   │   ├── User Name
│   │   │   └── User Email
│   │   ├── Settings Card
│   │   │   ├── Đơn vị tiền tệ
│   │   │   └── Quản lý danh mục
│   │   └── Account Actions Card
│   │       ├── Đăng xuất
│   │       └── Xóa tài khoản
│   └── Bottom Navigation
```

---

### 2. Category Management Layout (activity_category_management.xml)

#### Thay đổi:
- ✅ **Hiển thị theo section**: Tách riêng Chi tiêu và Thu nhập
- ✅ **Sử dụng 2 RecyclerView**: Một cho chi tiêu, một cho thu nhập
- ✅ **Material Card cho mỗi section**: Tạo phân cách rõ ràng
- ✅ **Label section**: "CHI TIÊU" và "THU NHẬP" màu xám nhạt

#### Layout Structure:
```
CoordinatorLayout
├── LinearLayout (Main Container)
│   ├── Header (Back + Title "Quản lý Danh mục")
│   └── ScrollView
│       └── LinearLayout
│           ├── "CHI TIÊU" Label
│           ├── MaterialCardView (Chi tiêu)
│           │   └── RecyclerView (rvExpenseCategories)
│           ├── "THU NHẬP" Label
│           └── MaterialCardView (Thu nhập)
│               └── RecyclerView (rvIncomeCategories)
```

---

### 3. CategoryManagementActivity.java

#### Thay đổi:
- ✅ **2 RecyclerView riêng biệt**: 
  - `rvExpenseCategories` cho danh mục chi tiêu
  - `rvIncomeCategories` cho danh mục thu nhập
  
- ✅ **2 Adapter riêng biệt**:
  - `expenseAdapter` cho chi tiêu
  - `incomeAdapter` cho thu nhập

- ✅ **2 List riêng biệt**:
  - `expenseCategories` cho chi tiêu
  - `incomeCategories` cho thu nhập

- ✅ **Load categories riêng biệt**:
  - Gọi Firebase 2 lần độc lập
  - Cập nhật adapter tương ứng

#### Code Changes:

**Before:**
```java
private RecyclerView rvCategories;
private CategoryManageAdapter adapter;
private List<Category> categories;

// Load tất cả categories vào 1 list
```

**After:**
```java
private RecyclerView rvExpenseCategories;
private RecyclerView rvIncomeCategories;
private CategoryManageAdapter expenseAdapter;
private CategoryManageAdapter incomeAdapter;
private List<Category> expenseCategories;
private List<Category> incomeCategories;

// Load riêng biệt cho từng loại
```

---

## 🎨 Design Details

### Colors & Styling:
- **Background**: #F5F5F5 (Light gray)
- **Card Background**: White
- **Primary Color**: Blue (#4A90E2 hoặc tương tự)
- **Section Label**: Text secondary color
- **Card Corner Radius**: 12dp
- **Card Elevation**: 2dp

### Typography:
- **Header Title**: 18sp, Bold (Inter Bold)
- **User Name**: 20sp, Bold (Inter Bold)
- **User Email**: 14sp, Regular (Inter Regular)
- **Menu Items**: 16sp, Medium (Inter Medium)
- **Section Labels**: 12sp, Medium (Inter Medium)

### Spacing:
- **Padding**: 16dp (general)
- **Card Margin Bottom**: 16dp
- **Section Margin Bottom**: 24dp (between sections)
- **Label Margin**: 12dp bottom, 4dp start

---

## 🧪 Testing Checklist

### Profile Screen:
- [ ] Header hiển thị đúng với nút Back và title
- [ ] User info card hiển thị tên và email
- [ ] Menu "Đơn vị tiền tệ" navigate đến FinancialSettings
- [ ] Menu "Quản lý danh mục" navigate đến CategoryManagement
- [ ] Button "Đăng xuất" hoạt động
- [ ] Button "Xóa tài khoản" hiển thị confirm dialog

### Category Management Screen:
- [ ] Header hiển thị "Quản lý Danh mục"
- [ ] Section "CHI TIÊU" hiển thị tất cả danh mục chi tiêu
- [ ] Section "THU NHẬP" hiển thị tất cả danh mục thu nhập
- [ ] Mỗi category hiển thị icon và tên
- [ ] Click vào category có thể edit/delete
- [ ] Scroll mượt mà giữa 2 section

---

## 📁 Files Changed

### Modified Files:
1. `app/src/main/res/layout/activity_profile.xml`
   - Removed search/more button from header
   - Kept same structure for user info and menu items

2. `app/src/main/res/layout/activity_category_management.xml`
   - Changed from single RecyclerView to dual RecyclerView
   - Added section labels and cards
   - Changed title to Vietnamese

3. `app/src/main/java/com/example/smartexpense/CategoryManagementActivity.java`
   - Added separate RecyclerViews for expense and income
   - Added separate adapters and lists
   - Updated loadCategories method to load separately

---

## 🚀 Build & Deploy

### Build Command:
```powershell
cd D:\codcdood\SmartExpense
.\gradlew.bat assembleDebug
```

### Expected Output:
```
BUILD SUCCESSFUL
APK: app/build/outputs/apk/debug/app-debug.apk
```

### Install Command:
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📝 Notes

### Compatibility:
- Android API 21+ (Android 5.0+)
- Sử dụng Material Design components
- Firebase Firestore cho data

### Performance:
- Nested RecyclerView với `nestedScrollingEnabled="false"`
- Scroll mượt mà với ScrollView bọc ngoài
- Lazy loading categories từ Firebase

### Future Improvements:
- [ ] Thêm FAB button để add new category
- [ ] Thêm swipe to delete cho category items
- [ ] Thêm drag & drop để reorder categories
- [ ] Thêm search/filter categories
- [ ] Thêm animation cho transitions

---

**Updated**: December 13, 2025  
**Status**: ✅ Completed & Ready to Test

