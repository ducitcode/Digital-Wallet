# Hướng dẫn test API bằng Postman

Tài liệu này liệt kê từng API để bạn tự tạo request trong Postman và chạy từng cái một.

## Chuẩn bị

- Chạy ứng dụng và PostgreSQL.
- Base URL mặc định: `http://localhost:8080`.
- Với request có JSON body, chọn **Body → raw → JSON**.
- Với API cần đăng nhập, thêm header:

```http
Authorization: Bearer <accessToken>
```

- Các response thành công thường có dạng `status`, `message`, `data`. Token nằm trong `data.accessToken` và `data.refreshToken`.

> Đăng ký tài khoản mới mỗi lần test. `username` và `email` phải chưa tồn tại trong database.

---

## 1. Đăng ký

**Method:** `POST`  
**URL:** `http://localhost:8080/api/v1/auth/register`  
**Auth:** Không cần  
**Body → raw → JSON:**

```json
{
  "username": "postman_user_01",
  "email": "postman_user_01@example.com",
  "phone": "0900000000",
  "password": "Test123456",
  "fullName": "Postman Test User"
}
```

Đăng ký thành công sẽ tạo tài khoản và trả luôn access token cùng refresh token. Hãy lưu hai giá trị này để dùng ở các bước sau.

---

## 2. Đăng nhập bằng username hoặc email

**Method:** `POST`  
**URL:** `http://localhost:8080/api/v1/auth/login`  
**Auth:** Không cần  
**Body → raw → JSON:**

```json
{
  "usernameOrEmail": "postman_user_01@example.com",
  "password": "Test123456"
}
```

Bạn cũng có thể thay email bằng `postman_user_01`. Response trả cặp token mới; dùng access token mới cho các API cần đăng nhập.

---

## 3. Lấy thông tin tài khoản hiện tại

**Method:** `GET`  
**URL:** `http://localhost:8080/api/v1/users/me`  
**Auth:** Bearer Token — nhập access token lấy từ đăng ký hoặc đăng nhập  
**Body:** Không có

Trong Postman, có thể chọn **Authorization → Bearer Token** rồi dán access token.

---

## 4. Cập nhật thông tin tài khoản

**Method:** `PUT`  
**URL:** `http://localhost:8080/api/v1/users/me`  
**Auth:** Bearer Token — access token  
**Body → raw → JSON:**

```json
{
  "fullName": "Postman Updated User",
  "phone": "0912345678"
}
```

Có thể gửi một trong hai trường hoặc cả hai. Gửi `{}` thì không thay đổi thông tin nào.

---

## 5. Tạo PIN giao dịch

**Method:** `POST`  
**URL:** `http://localhost:8080/api/v1/pin`  
**Auth:** Bearer Token — access token  
**Body → raw → JSON:**

```json
{
  "pin": "123456"
}
```

PIN phải gồm đúng 6 chữ số. Mỗi tài khoản chỉ tạo PIN một lần; nếu đã tạo rồi, API sẽ báo xung đột.

---

## 6. Xác minh PIN

**Method:** `POST`  
**URL:** `http://localhost:8080/api/v1/pin/verify`  
**Auth:** Bearer Token — access token  
**Body → raw → JSON:**

```json
{
  "pin": "123456"
}
```

PIN đúng trả về `data: true`. PIN sai làm tăng số lần nhập sai; mặc định nhập sai 5 lần sẽ khóa PIN 15 phút.

---

## 7. Đổi PIN

**Method:** `PUT`  
**URL:** `http://localhost:8080/api/v1/pin`  
**Auth:** Bearer Token — access token  
**Body → raw → JSON:**

```json
{
  "oldPin": "123456",
  "pin": "654321"
}
```

`oldPin` là PIN hiện tại; `pin` là PIN mới, gồm đúng 6 chữ số. Sau khi đổi, thử lại API xác minh PIN bằng PIN mới.

---

## 8. Làm mới token

**Method:** `POST`  
**URL:** `http://localhost:8080/api/v1/auth/refresh`  
**Auth:** Không cần access token  
**Body → raw → JSON:**

```json
{
  "refreshToken": "<refreshToken hiện tại>"
}
```

Refresh token cũ sẽ bị thu hồi khi làm mới thành công. Hãy thay cả access token và refresh token đang lưu bằng hai token mới trong response. Nếu tiếp tục gửi refresh token cũ, request sẽ thất bại.

---

## 9. Đổi mật khẩu

**Method:** `POST`  
**URL:** `http://localhost:8080/api/v1/auth/change-password`  
**Auth:** Bearer Token — access token  
**Body → raw → JSON:**

```json
{
  "oldPassword": "Test123456",
  "newPassword": "Test654321"
}
```

Mật khẩu mới phải có ít nhất 6 ký tự và khác mật khẩu cũ. API này thu hồi toàn bộ refresh token của tài khoản. Sau đó đăng nhập lại bằng mật khẩu mới để nhận cặp token mới.

---

## 10. Đăng xuất

**Method:** `POST`  
**URL:** `http://localhost:8080/api/v1/auth/logout`  
**Auth:** Không cần access token  
**Body → raw → JSON:**

```json
{
  "refreshToken": "<refreshToken hiện tại>"
}
```

API thu hồi refresh token được gửi. Access token hiện tại không bị thu hồi ngay và vẫn có thể hoạt động đến khi hết hạn.

---

## Thứ tự test gợi ý

1. Đăng ký **hoặc** đăng nhập để lấy token.
2. Dùng access token gọi xem/cập nhật hồ sơ.
3. Tạo PIN → xác minh PIN → đổi PIN → xác minh PIN mới.
4. Dùng refresh token để làm mới; cập nhật cả hai token theo response.
5. Đổi mật khẩu bằng access token; đăng nhập lại bằng mật khẩu mới.
6. Đăng xuất bằng refresh token vừa nhận khi đăng nhập lại.

Nếu test đăng ký lần nữa, đổi username và email sang giá trị mới để tránh lỗi trùng dữ liệu.
