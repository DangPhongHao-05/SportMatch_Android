# Dự án: SPM - Bản đồ thể thao

**SPM** là một ứng dụng di động giúp những người yêu thích thể thao dễ dàng tìm thấy nhau. Bạn có thể bật bản đồ để quét xem xung quanh mình có ai đang tìm người đá bóng, đánh cầu lông hay bóng chuyền không để "nhảy vào" chốt kèo và đi chơi ngay.

---

## 1. Các tính năng chính

*   **Đăng nhập nhanh**: Xác thực qua số điện thoại bằng mã OTP thông qua Firebase Authentication.
*   **Tìm kèo quanh đây**: Xem bản đồ để biết các nhóm hoặc cá nhân nào đang thiếu người ở gần vị trí của bạn (Dự kiến).
*   **Chat trực tiếp**: Nhắn tin ngay trên ứng dụng để trao đổi về thời gian, địa điểm và trình độ chơi.
*   **Quản lý hồ sơ**: Lưu trữ thông tin cá nhân và số điện thoại liên lạc để thuận tiện kết nối.

---

## 2. Công nghệ sử dụng

### Frontend (Mobile App)
*   **Ngôn ngữ**: Kotlin
*   **Giao diện**: Jetpack Compose (Modern UI)
*   **Bản đồ**: Google Maps SDK
*   **Kết nối mạng**: Retrofit & OkHttp
*   **Xác thực**: Firebase Auth (OTP)

### Backend (API Server)
*   **Ngôn ngữ**: C# (.NET 9)
*   **Cơ sở dữ liệu**: MySQL
*   **Công nghệ**: ASP.NET Core API, Entity Framework Core
*   **Xử lý bản đồ**: NetTopologySuite (hỗ trợ tính toán khoảng cách tọa độ)

---

## 3. Cấu trúc thư mục

### Android Project
```text
com.example.sportmatch/
├── data/                          # Xử lý dữ liệu và kết nối API
│   ├── api/                       # Khai báo API Service, Retrofit
│   ├── dto/                       # DTO dùng để gửi/nhận dữ liệu
│   ├── model/                     # Model dữ liệu của ứng dụng
│   └── repository/                # Trung gian xử lý dữ liệu giữa API và ViewModel
│
├── navigation/                    # Điều hướng giữa các màn hình
│
├── ui/                            # Giao diện người dùng
│   ├── auth/                      # Đăng nhập, xác thực người dùng
│   ├── map/                       # Bản đồ, vị trí sân đấu
│   ├── match/                     # Ghép trận, danh sách trận đấu
│   ├── message/                   # Chat, nhắn tin giữa người chơi
│   └── profile/                   # Hồ sơ cá nhân người dùng
│
└── MainActivity.kt                # Điểm khởi chạy chính của ứng dụng
```

### Backend Project (.NET)
```text
SportMatchAPI/
├── Controllers/        # Xử lý các yêu cầu HTTP (Auth, Location, Chat)
├── Data/               # Cấu hình kết nối Cơ sở dữ liệu (AppDbContext)
├── Models/             # Định nghĩa các bảng dữ liệu hệ thống
├── DTO/                # Các mẫu dữ liệu truyền tải giữa App và API
└── Program.cs          # Cấu hình khởi tạo dịch vụ và Middleware
```
---
### Nhóm phát triển:
* Đặng Phong Hào
* Bùi Tấn Khang
* Võ Thị Kiều Trang
