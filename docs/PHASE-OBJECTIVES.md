# Digital Wallet & QR Payment System — Mục tiêu theo Phase

Tài liệu mô tả **mục tiêu**, **phạm vi**, **chức năng cần làm** và **kết quả mong đợi** cho từng phase (0–30). Dùng làm checklist triển khai và theo dõi tiến độ.

---

## Tổng quan dự án

| Hạng mục | Nội dung |
|----------|----------|
| **Tên** | Digital Wallet & QR Payment System |
| **Mục đích** | Hệ thống ví điện tử mô phỏng, hướng **production-like**, thể hiện kỹ năng Java Backend thực tế |
| **Kiến trúc giai đoạn đầu** | Modular Monolith |
| **Frontend** | React + TypeScript |
| **Backend** | Java 21 + Spring Boot 3.x |
| **Database** | PostgreSQL |
| **Cache / hỗ trợ** | Redis |
| **Triển khai** | Docker, CI/CD (GitHub Actions) |
| **Tiền tệ v1** | VND (chưa multi-currency) |

### Module hệ thống (22 nhóm)

1. Authentication & Authorization  
2. User Management  
3. Wallet Management  
4. Deposit  
5. Withdrawal  
6. Money Transfer  
7. Transaction Management  
8. Ledger  
9. QR Payment  
10. Transaction PIN  
11. Notification  
12. Idempotency  
13. Redis Cache  
14. Rate Limiting  
15. Audit Log  
16. Admin  
17. Frontend  
18. Testing  
19. Docker  
20. CI/CD  
21. Monitoring  
22. Documentation  

### Actor & phân quyền

| Actor | Mô tả |
|-------|--------|
| **USER** | Người dùng ví: đăng ký, đăng nhập, ví, nạp/rút/chuyển, QR, lịch sử, PIN, profile |
| **ADMIN** | Quản trị: user, wallet, giao dịch, audit log, khóa/mở khóa |

**Role:** `USER`, `ADMIN`

### Trạng thái & loại giao dịch (chuẩn toàn hệ thống)

**Transaction status:** `PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`, `CANCELLED`

**Transaction type:** `DEPOSIT`, `WITHDRAW`, `TRANSFER`, `PAYMENT`, `REFUND`

**User status:** `ACTIVE`, `LOCKED`, `INACTIVE`

**Wallet status:** `ACTIVE`, `LOCKED`, `CLOSED`

### Business rules cốt lõi (áp dụng xuyên suốt)

- Số dư ví **không âm**
- Số tiền giao dịch **> 0**
- **Không** chuyển tiền cho chính mình
- Người gửi **đủ số dư**; user và wallet phải **ACTIVE** (trừ deposit mô phỏng theo rule riêng)
- **Transaction PIN** đúng khi rút/chuyển/thanh toán (theo phase triển khai)
- **Không** xử lý cùng một giao dịch hai lần; **Idempotency-Key** cho API ghi tiền
- Mọi thay đổi số dư **phải có** bản ghi Transaction + Ledger (double-entry)
- Tổng **DEBIT = CREDIT** trên mỗi giao dịch tài chính
- **Không** cho client/Frontend cập nhật balance trực tiếp

### Mốc hoàn thành quan trọng

| Milestone | Thời gian ước tính | Nội dung chính |
|-----------|-------------------|----------------|
| **M1 – MVP** | ~2 tuần | Register, Login, JWT, Wallet, Deposit, Withdraw, Transfer, Transaction History |
| **M2 – Production-like Backend** | ~4–5 tuần | Ledger, Idempotency, Lock, PIN, Redis, Rate limit, Audit, Test, Swagger |
| **M3 – Complete Product** | ~6–8 tuần | React, QR, Notification, Admin, Docker, CI/CD, Monitoring |
| **M4 – Advanced** | ~8–10 tuần | Performance test, concurrent test, Prometheus/Grafana, security review, deploy |

**Tổng thời gian nếu làm đủ 30 phase:** khoảng **65–85 ngày làm việc** (không cần chờ hết mới có sản phẩm — theo milestone).

---

## Phase 0 — Phân tích yêu cầu và thiết kế

**Thời gian:** 2 ngày  

**Mục tiêu:** Xác định chính xác hệ thống cần làm gì **trước khi viết code**.

### Công việc chi tiết

#### 0.1 — Actor & use case

- Liệt kê **USER**: đăng ký, đăng nhập, xem ví, nạp, rút, chuyển, nhận tiền, QR (tạo/quét), lịch sử, profile, đổi PIN, phiên/thiết bị (nếu có trong scope)
- Liệt kê **ADMIN**: xem/tìm user, khóa/mở user, xem/khóa wallet, xem/tìm giao dịch, audit log, theo dõi hệ thống
- Vẽ **Use Case Diagram** (USER / ADMIN)

#### 0.2 — Domain model sơ bộ

- Transaction status & type (như mục tổng quan)
- Currency: VND v1; ghi chú mở rộng USD/EUR sau

#### 0.3 — Business rules document

- Thu thập và chuẩn hóa toàn bộ rule (balance, PIN, idempotency, ledger, wallet/user status, giới hạn số tiền nạp/rút nếu có)
- Phân loại rule: **validation**, **authorization**, **financial invariant**

#### 0.4 — API list (REST v1)

- Nhóm: Auth, User, Wallet, Deposit, Withdraw, Transfer, Transaction, QR, PIN, Notification, Admin, Audit (skeleton endpoint + method)
- Quy ước: prefix `/api/v1`, JWT, header `Idempotency-Key` cho mutation tài chính

#### 0.5 — ERD sơ bộ

- Entities: users, wallets, transactions, ledger_entries, idempotency_keys, refresh_tokens, transaction_pins, qr_codes, notifications, audit_logs
- Quan hệ, khóa, index gợi ý (wallet_number unique, transaction_code unique, v.v.)

#### 0.6 — Flow diagram

- Register → wallet tự tạo
- Login / refresh / logout
- Deposit / Withdraw / Transfer (happy path + fail)
- QR: personal / payment / resolve / pay
- Admin lock user/wallet

### Deliverables Phase 0

- [ ] Requirement Document  
- [ ] Use Case (diagram + mô tả ngắn)  
- [ ] Business Rule (bảng rule + mã lỗi gợi ý)  
- [ ] API list  
- [ ] ERD sơ bộ  
- [ ] Flow Diagram  

---

## Phase 1 — Khởi tạo project Backend

**Thời gian:** 1–2 ngày  

**Mục tiêu:** Spring Boot project chuẩn, cấu trúc package và nền tảng xử lý lỗi/logging.

### Chức năng / công việc

#### 1.1 — Tạo project

- Java **21**, Spring Boot **3.x**
- Dependencies: Web, Data JPA, Security, Validation, PostgreSQL Driver, Flyway, Lombok, Actuator

#### 1.2 — Cấu hình môi trường

- `application.yml`, `application-dev.yml`, `application-test.yml`, `application-prod.yml`
- Cấu hình: DB URL, user/pass (env), JWT secret, Redis, logging, server port — **không hard-code password**

#### 1.3 — Package structure

Ví dụ root `com.wallet`:

`auth`, `user`, `wallet`, `transaction`, `ledger`, `transfer`, `deposit`, `withdrawal`, `qr`, `notification`, `idempotency`, `audit`, `admin`, `common`, `config`, `security`

#### 1.4 — Global exception handling

- Exception: `BusinessException`, `ResourceNotFoundException`, `UnauthorizedException`, `InsufficientBalanceException`, `InvalidTransactionException`
- `GlobalExceptionHandler` — response thống nhất: `code`, `message`, `timestamp`, `traceId`

#### 1.5 — Logging

- Request / response / error / transaction log (structured nếu có thể)
- **Không log:** password, PIN, JWT, dữ liệu nhạy cảm

### Deliverables Phase 1

- [ ] Project build được (`mvnw clean verify` hoặc tương đương)  
- [ ] Profile dev/test/prod tách biệt  
- [ ] Exception API format thống nhất  
- [ ] Logging policy documented trong code hoặc README ngắn  

---

## Phase 2 — Database & data model

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Schema PostgreSQL + Flyway, entity JPA, hỗ trợ transaction và ledger.

### Bảng & trường cần triển khai

| Bảng | Điểm quan trọng |
|------|------------------|
| **users** | id, username, email, phone, password_hash, full_name, status, role, created_at, updated_at |
| **wallets** | id, wallet_number, user_id, balance, currency, status, **version** (optimistic lock), timestamps |
| **transactions** | id, transaction_code (unique), type, status, source/dest wallet_id, amount, currency, description, reference, created_at, completed_at |
| **ledger_entries** | id, transaction_id, wallet_id, entry_type (DEBIT/CREDIT), amount, created_at |
| **idempotency_keys** | id, key, user_id, request_hash, response, status, created_at, expired_at |
| **refresh_tokens** | id, user_id, token, expires_at, revoked, created_at |
| **transaction_pins** | user_id, pin_hash, failed_attempts, locked_until, updated_at |
| **qr_codes** | id, qr_code, type, wallet_id, amount, currency, reference, status, expired_at, created_at |
| **notifications** | id, user_id, title, content, type, is_read, created_at |
| **audit_logs** | id, user_id, action, resource, resource_id, ip_address, user_agent, created_at |

### Công việc chi tiết

- Flyway migration V1… (tạo bảng, constraint, index)
- Entity + enum (status, role, entry_type, …)
- Repository interface (Spring Data JPA)
- Seed tùy chọn: admin user (dev only, không commit password thật)

### Deliverables Phase 2

- [ ] Migration chạy sạch trên PostgreSQL  
- [ ] Entity map đúng schema  
- [ ] ERD cập nhật khớp migration  

---

## Phase 3 — User registration & authentication

**Thời gian:** 3 ngày  

**Mục tiêu:** Authentication hoàn chỉnh: register, login, JWT, refresh, logout, đổi password.

### API & chức năng

| API | Chức năng chi tiết |
|-----|---------------------|
| `POST /api/v1/auth/register` | username, email, phone, password, fullName — unique checks, password strength, BCrypt hash |
| `POST /api/v1/auth/login` | Validate credentials → access token + refresh token |
| `POST /api/v1/auth/refresh` | Refresh token hợp lệ → access token mới |
| `POST /api/v1/auth/logout` | Revoke refresh token |
| `POST /api/v1/auth/change-password` | Old + new password, hash mới |

### JWT

- Access token claims: `userId`, `username`, `role`
- Security filter chain: public auth endpoints, protected `/api/v1/**`

### Deliverables Phase 3

- [ ] Register/Login/Refresh/Logout/Change password hoạt động  
- [ ] Refresh token lưu DB (revoke được)  
- [ ] Test cơ bản auth (unit hoặc integration)  

---

## Phase 4 — User management

**Thời gian:** 2 ngày  

**Mục tiêu:** Profile cá nhân cho USER.

### API & rule

| API | Chi tiết |
|-----|----------|
| `GET /api/v1/users/me` | Xem profile (không trả password_hash) |
| `PUT /api/v1/users/me` | Cập nhật fullName, phone, email (validate unique) |

**Không cho user tự đổi:** role, status, balance.

### Deliverables Phase 4

- [ ] GET/PUT profile với authorization đúng user  
- [ ] Validation email/phone trùng  

---

## Phase 5 — Wallet management

**Thời gian:** 2 ngày  

**Mục tiêu:** Mỗi user có ví; xem số dư và trạng thái ví.

### Chức năng

- **Sau register:** tự động tạo wallet `ACTIVE`, currency VND, `wallet_number` unique
- `GET /api/v1/wallets/me` — wallet number, balance, currency, status
- Rule: wallet `LOCKED` / `CLOSED` → không transfer, withdraw, payment (deposit policy ghi rõ trong BR)

### Deliverables Phase 5

- [ ] Auto-create wallet on register  
- [ ] API xem ví của chính mình  
- [ ] Status enum enforced ở service layer (skeleton cho phase sau)  

---

## Phase 6 — Deposit (nạp tiền mô phỏng)

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Mô phỏng nạp tiền vào ví (không tích hợp ngân hàng thật).

### API

`POST /api/v1/deposits`

**Input:** amount, description, idempotencyKey (header/body theo thiết kế Phase 0)

**Validate:** amount > 0, wallet ACTIVE, giới hạn max deposit (nếu định nghĩa)

### Flow nghiệp vụ

1. Tạo transaction type DEPOSIT  
2. Cập nhật balance (+)  
3. Ghi ledger CREDIT  
4. Status SUCCESS (hoặc PENDING→SUCCESS nếu async — v1 thường sync)  
5. `@Transactional` — ledger fail → rollback balance  

### Deliverables Phase 6

- [ ] Deposit end-to-end với transaction record  
- [ ] Idempotency hook (có thể stub đến Phase 14)  

---

## Phase 7 — Withdrawal (rút tiền)

**Thời gian:** 2 ngày  

**Mục tiêu:** Rút tiền có kiểm tra số dư và ví.

### API

`POST /api/v1/withdrawals` — amount, description, idempotencyKey; sau Phase 12 thêm **PIN**

### Flow

Check wallet → check balance → lock/update balance → transaction WITHDRAW → ledger DEBIT → commit

**Fail:** balance < amount → FAILED / exception `INSUFFICIENT_BALANCE`

### Deliverables Phase 7

- [ ] Withdraw không làm balance âm  
- [ ] Transaction + ledger đồng bộ  

---

## Phase 8 — Money transfer

**Thời gian:** 4–5 ngày  

**Mục tiêu:** Module cốt lõi — chuyển tiền giữa hai ví.

### API

`POST /api/v1/transfers`

**Body:** destinationWallet (số ví), amount, description  
**Header:** `Idempotency-Key`

### Flow

Validate sender/recipient → không self-transfer → balance → lock wallets → debit sender, credit receiver → transaction TRANSFER → 2 ledger entries → commit → notification (Phase 17)

### Từ chối khi

Self-transfer, amount ≤ 0, wallet không tồn tại, wallet/user locked, insufficient balance, PIN sai (Phase 12), duplicate idempotency

### Deliverables Phase 8

- [ ] Transfer thành công giữa 2 user  
- [ ] Transaction code unique, lưu source/dest wallet  

---

## Phase 9 — Concurrency & data consistency

**Thời gian:** 3–4 ngày  

**Mục tiêu:** Xử lý race condition khi nhiều request đồng thời.

### Kỹ thuật

- **Optimistic locking:** `@Version` trên wallet  
- **Pessimistic locking:** `SELECT … FOR UPDATE` khi cần cho financial path  
- **Isolation level:** chọn READ COMMITTED / REPEATABLE READ và document lý do  
- Retry policy khi optimistic conflict (giới hạn số lần)

### Test bắt buộc

- 100 concurrent transfer/request — balance không âm, conservation of money (tổng trước = tổng sau)

### Deliverables Phase 9

- [ ] Concurrent test pass  
- [ ] Document locking strategy trong docs kỹ thuật  

---

## Phase 10 — Double-entry ledger

**Thời gian:** 3 ngày  

**Mục tiêu:** Mọi biến động tiền đều qua ledger cân bằng.

### Quy tắc

- Deposit: CREDIT wallet user  
- Withdraw: DEBIT wallet user  
- Transfer: DEBIT sender, CREDIT receiver — **tổng DEBIT = tổng CREDIT**  
- **Cấm** chỉ `UPDATE balance` không có transaction + ledger

### Công việc

- Service layer ledger helper (tạo entries theo type)  
- Reconciliation query (optional): sum ledger vs balance audit  
- Admin/user detail: hiển thị ledger entries (Phase 11)

### Deliverables Phase 10

- [ ] Mọi API tiền (6–8) đều ghi ledger  
- [ ] Unit test invariant DEBIT=CREDIT  

---

## Phase 11 — Transaction history & detail

**Thời gian:** 2 ngày  

**Mục tiêu:** Tra cứu giao dịch cho USER (và chuẩn bị cho Admin).

### API

| API | Chi tiết |
|-----|----------|
| `GET /api/v1/transactions` | Pagination, sort, filter: type, status, from/to date, amount range — chỉ giao dịch liên quan user |
| `GET /api/v1/transactions/{transactionCode}` | Code, type, amount, sender/receiver, status, mô tả, thời gian, **ledger entries** |

### Deliverables Phase 11

- [ ] List + detail đúng phân quyền USER  
- [ ] Performance: index theo user_id/wallet_id, created_at  

---

## Phase 12 — Transaction PIN

**Thời gian:** 2 ngày  

**Mục tiêu:** PIN 6 số cho giao dịch nhạy cảm.

### API

- `POST /api/v1/pin` — tạo/đổi PIN (BCrypt hash)  
- Verify PIN trong: withdraw, transfer, QR payment

### Brute-force

- Sai 5 lần → `locked_until` (khoảng thời gian cấu hình được)

### Deliverables Phase 12

- [ ] PIN không lưu plaintext  
- [ ] Transfer/withdraw reject khi PIN sai hoặc locked  

---

## Phase 13 — QR payment

**Thời gian:** 4–5 ngày  

**Mục tiêu:** QR nhận tiền, QR thanh toán có số tiền, resolve và thanh toán.

### Loại QR

1. **Personal QR** — wallet identifier cố định; payer nhập amount  
2. **Payment QR** — walletId, amount, currency, reference, expiresAt; payer không nhập amount

### API

- `POST /api/v1/qr/personal`  
- `POST /api/v1/qr/payment`  
- `POST /api/v1/qr/resolve` — decode/lookup → thông tin recipient + amount (nếu có)  
- Thanh toán: dùng transfer/payment flow type `PAYMENT` + PIN + idempotency

### Expiration

Payment QR hết hạn (vd. 5 phút) → không thanh toán được

### Deliverables Phase 13

- [ ] Tạo/resolve QR  
- [ ] Pay qua QR → transaction PAYMENT + ledger + notify  

---

## Phase 14 — Idempotency

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Tránh double-spend khi client retry.

### Cơ chế

- Client gửi `Idempotency-Key` (UUID)  
- Lưu `idempotency_keys`: key + user_id + request_hash + response snapshot + status + TTL  
- Request trùng key + cùng hash → trả response đã lưu, **không** thực thi lại  
- Key khác hash → 409 Conflict (policy rõ ràng)

### Áp dụng cho

Deposit, Withdraw, Transfer, QR payment

### Deliverables Phase 14

- [ ] Test: 2 request cùng key → 1 transaction  
- [ ] Expired key cleanup (job hoặc TTL DB)  

---

## Phase 15 — Redis

**Thời gian:** 3 ngày  

**Mục tiêu:** Redis cho idempotency nhanh, rate limit, session/token tùy chọn, cache có kiểm soát.

### Use cases

| Use case | Ghi chú |
|----------|---------|
| Idempotency cache | Key ngắn hạn trước khi persist DB |
| Rate limiting | Counter theo user/IP |
| Refresh token / session | Optional blacklist hoặc session store |
| Cache profile | Invalidate khi update user |
| **Không** cache balance bừa bãi | Tránh stale balance |

### Deliverables Phase 15

- [ ] Redis config dev/docker  
- [ ] Integration idempotency + rate limit (Phase 16)  

---

## Phase 16 — Rate limiting

**Thời gian:** 1–2 ngày  

**Mục tiêu:** Giảm spam, brute force, abuse API.

### Policy ví dụ

- Login: **5 req/phút/IP**  
- Transfer: **30 req/phút/user**  
- Register: giới hạn theo IP

### Hành vi

- Vượt ngưỡng → HTTP 429 + header Retry-After (nếu có)

### Deliverables Phase 16

- [ ] Rate limit login + transfer  
- [ ] Test hoặc doc cấu hình threshold  

---

## Phase 17 — Notification

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Thông báo in-app khi có giao dịch.

### Sự kiện

Transfer/Payment/Deposit/Withdraw success → notify sender và/hoặc receiver (nội dung tiền + loại)

### API

- `GET /api/v1/notifications` — pagination, unread filter  
- `PUT /api/v1/notifications/{id}/read`

### Mở rộng (optional)

Email, WebSocket — không bắt buộc v1

### Deliverables Phase 17

- [ ] Notification persist sau giao dịch thành công  
- [ ] Mark read  

---

## Phase 18 — Audit log

**Thời gian:** 2 ngày  

**Mục tiêu:** Ghi vết hành động quan trọng cho compliance/debug.

### Hành động ghi log

Login, logout, change password, create wallet, deposit, withdraw, transfer, create QR, update profile, admin lock user/wallet, …

### Trường

user_id, action, resource, resource_id, ip_address, user_agent, created_at

### Deliverables Phase 18

- [ ] Aspect hoặc service gọi audit không block main flow (async optional)  
- [ ] Không ghi PIN/password  

---

## Phase 19 — Admin module

**Thời gian:** 3–4 ngày  

**Mục tiêu:** API ADMIN — quản lý user, wallet, transaction, audit.

### Chức năng chi tiết

**User:** search, view, lock, unlock  
**Wallet:** search, view balance, lock, unlock  
**Transaction:** search, filter status/type/date, view detail + ledger  
**Audit:** list, filter user/action/time  

### Security

- `@PreAuthorize("hasRole('ADMIN')")` hoặc tương đương  
- Audit mọi admin action

### Deliverables Phase 19

- [ ] Admin APIs tách prefix (vd. `/api/v1/admin/...`)  
- [ ] USER không gọi được admin endpoints  

---

## Phase 20 — Frontend (React + TypeScript)

**Thời gian:** 7–10 ngày  

**Mục tiêu:** UI đầy đủ flow người dùng.

### Trang & chức năng

| Trang | Chi tiết |
|-------|----------|
| Login | Username/email, password, logout, refresh token flow |
| Register | fullName, username, email, phone, password, confirm |
| Dashboard | User, wallet number, balance, currency, quick actions |
| Deposit | Nhập amount, confirm, hiển thị kết quả |
| Withdraw | Amount + PIN, confirm |
| Transfer | Wallet number, amount, description, PIN |
| QR Receive | Personal QR, copy wallet, download QR |
| QR Scanner | Camera → resolve → confirm → PIN → pay |
| Transaction history | List, filter, pagination, incoming/outgoing |
| Transaction detail | Full info + status |
| Profile | View/update |
| Security | Change password, change PIN, logout, session (nếu BE hỗ trợ) |

### Kỹ thuật FE

- API client với interceptors (JWT, 401 refresh)  
- Idempotency-Key khi gọi mutation tiền  
- Error mapping từ `code` backend

### Deliverables Phase 20

- [ ] Chạy local against backend dev  
- [ ] Responsive cơ bản  

---

## Phase 21 — Testing

**Thời gian:** 5–7 ngày  

**Mục tiêu:** Coverage các luồng tài chính và auth — điểm nhấn CV Backend.

### Loại test

- **Unit:** service, validator, business rules, utils — JUnit 5, Mockito  
- **Integration:** Controller → Service → Repository → DB — **Testcontainers + PostgreSQL** (không chỉ H2 cho toàn bộ)

### Case bắt buộc

| Case | Kỳ vọng |
|------|---------|
| Transfer success | Balance A/B đúng |
| Insufficient balance | FAILED |
| Self-transfer | FAILED |
| Wallet locked | FAILED |
| Wrong PIN | FAILED |
| Duplicate idempotency | 1 transaction |
| Concurrent 100 requests | Không âm balance, conservation |
| Ledger fail | Rollback balance |

### Deliverables Phase 21

- [ ] CI chạy unit + integration  
- [ ] Báo cáo coverage (optional threshold)  

---

## Phase 22 — API documentation (Swagger/OpenAPI)

**Thời gian:** 1–2 ngày  

**Mục tiêu:** Document toàn bộ API public + admin.

### Nội dung mỗi endpoint

Method, path, request body, response, error codes, HTTP status, security scheme (Bearer JWT)

### Deliverables Phase 22

- [ ] Swagger UI `/swagger-ui.html` (hoặc springdoc path)  
- [ ] Export OpenAPI JSON  

---

## Phase 23 — Docker

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Chạy full stack bằng Docker Compose.

### Services

- wallet-backend  
- wallet-frontend  
- postgres  
- redis  

### Deliverables Phase 23

- [ ] `docker compose up` — app usable  
- [ ] Flyway chạy khi backend start  
- [ ] `.env.example` không chứa secret thật  

---

## Phase 24 — CI/CD (GitHub Actions)

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Pipeline trên mỗi push/PR.

### Pipeline

Checkout → Build → Unit test → Integration test → Build Docker image

### Deliverables Phase 24

- [ ] Workflow YAML trong repo  
- [ ] Badge README (optional)  

---

## Phase 25 — Monitoring

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Quan sát health và HTTP metrics.

### Spring Boot Actuator

Health, metrics, info — bảo vệ endpoint prod

### Mở rộng (M4)

Prometheus scrape, Grafana dashboard

### Deliverables Phase 25

- [ ] `/actuator/health` up khi DB/Redis OK  
- [ ] Document endpoint monitoring  

---

## Phase 26 — Performance test

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Đo tải với JMeter hoặc k6.

### Kịch bản

- Login: 100 concurrent users  
- Wallet inquiry: 500 concurrent  
- Transfer: 100 concurrent  

### Metric

Avg, P95, P99, throughput, error rate — ghi vào doc kết quả

### Deliverables Phase 26

- [ ] Script k6/JMeter trong repo  
- [ ] Báo cáo ngắn performance  

---

## Phase 27 — Security review

**Thời gian:** 2 ngày  

**Mục tiêu:** Checklist bảo mật trước demo/deploy.

### Hạng mục kiểm tra

Password hashing, JWT validation/expiry, RBAC, SQL injection (parameterized), input validation, rate limit, brute force PIN/login, CORS, sensitive logging, idempotency, admin access control

### Deliverables Phase 27

- [ ] Security checklist signed-off trong docs  
- [ ] Fix critical findings  

---

## Phase 28 — Frontend + Backend integration

**Thời gian:** 2–3 ngày  

**Mục tiêu:** E2E manual/automated qua UI.

### Flow đầy đủ

Register → Login → Wallet → Deposit → Transfer → QR Payment → History → Logout

### Deliverables Phase 28

- [ ] Test script hoặc checklist E2E  
- [ ] Bug fix blocking demo  

---

## Phase 29 — Demo end-to-end

**Thời gian:** 1 ngày  

**Mục tiêu:** Kịch bản demo phỏng vấn/CV.

### Setup dữ liệu

- User A (Nguyễn): balance ~10.000.000 VND  
- User B (Lan): ~1.000.000 VND  

### Kịch bản demo

1. Deposit 5.000.000 → balance cập nhật  
2. Transfer 500.000 A→B  
3. Lan tạo Payment QR 150.000 → Nguyễn scan + PIN → SUCCESS  
4. Gửi 2 lần cùng Idempotency-Key → 1 lần trừ tiền  
5. Concurrent requests → balance hợp lệ  

### Deliverables Phase 29

- [ ] Script demo (video hoặc slide steps)  
- [ ] Seed data script (dev/demo profile)  

---

## Phase 30 — Documentation (README & docs)

**Thời gian:** 2–3 ngày  

**Mục tiêu:** Repo tự giải thích cho reviewer/recruiter.

### README bắt buộc

1. Project overview  
2. Architecture diagram (FE → BE → PostgreSQL/Redis)  
3. Database ERD (link hoặc embed)  
4. Link Swagger  
5. Installation (`git clone`, `docker compose up`)  
6. Demo accounts (không password thật trên repo)  
7. **Technical highlights:** Security, JWT, PostgreSQL, Redis, `@Transactional`, optimistic/pessimistic lock, double-entry ledger, idempotency, QR, rate limiting, Testcontainers, Docker, CI/CD  

### Deliverables Phase 30

- [ ] README hoàn chỉnh  
- [ ] `docs/` đồng bộ Phase 0 + phase objectives (file này)  

---

## Flow mục tiêu cuối cùng (demo interview)

```
User A đăng ký → Login → Wallet → Deposit 10.000.000 VND
User B đăng ký → Wallet → Hiển thị QR
User A scan QR → Resolve → Nhập tiền + PIN
→ JWT + wallet + balance + idempotency + lock
→ Debit A / Credit B → Transaction + Ledger → Commit
→ Notification → Response SUCCESS → FE cập nhật balance
→ Cả hai xem Transaction History
→ Admin tra cứu giao dịch
→ Audit Log ghi toàn bộ
```

---

## Bảng tóm tắt thời gian

| Phase | Nội dung | Thời gian |
|-------|----------|-----------|
| 0 | Requirement & Design | 2 ngày |
| 1 | Backend Initialization | 1–2 ngày |
| 2 | Database | 2–3 ngày |
| 3 | Authentication | 3 ngày |
| 4 | User Management | 2 ngày |
| 5 | Wallet | 2 ngày |
| 6 | Deposit | 2–3 ngày |
| 7 | Withdrawal | 2 ngày |
| 8 | Transfer | 4–5 ngày |
| 9 | Concurrency | 3–4 ngày |
| 10 | Double Entry Ledger | 3 ngày |
| 11 | Transaction History | 2 ngày |
| 12 | Transaction PIN | 2 ngày |
| 13 | QR Payment | 4–5 ngày |
| 14 | Idempotency | 2–3 ngày |
| 15 | Redis | 3 ngày |
| 16 | Rate Limiting | 1–2 ngày |
| 17 | Notification | 2–3 ngày |
| 18 | Audit Log | 2 ngày |
| 19 | Admin | 3–4 ngày |
| 20 | Frontend | 7–10 ngày |
| 21 | Testing | 5–7 ngày |
| 22 | Swagger | 1–2 ngày |
| 23 | Docker | 2–3 ngày |
| 24 | CI/CD | 2–3 ngày |
| 25 | Monitoring | 2–3 ngày |
| 26 | Performance Test | 2–3 ngày |
| 27 | Security Review | 2 ngày |
| 28 | FE/BE Integration | 2–3 ngày |
| 29 | Demo | 1 ngày |
| 30 | Documentation | 2–3 ngày |

---

## Phụ lục — Checklist chức năng cuối cùng theo vai trò

### USER

- **Auth:** Register, Login, Logout, Refresh, Change password  
- **Profile:** View, Update  
- **Wallet:** Create (auto), View, Balance, Status  
- **Money:** Deposit, Withdraw, Transfer, Receive  
- **QR:** Personal, Payment, Scan, Resolve, Expiration  
- **Transaction:** History, Detail, Filter, Search, Pagination, Status  
- **Security:** JWT, BCrypt, PIN, PIN retry, Rate limiting  
- **Notification:** Transaction alerts, Read/unread  

### ADMIN

- **User:** Search, View, Lock, Unlock  
- **Wallet:** Search, View, Lock, Unlock  
- **Transaction:** Search, Filter, View, Ledger  
- **Audit:** View, Search, Filter  

### Backend advanced (mục tiêu kỹ thuật)

Spring Boot, Security, JWT, PostgreSQL, JPA, Flyway, Redis, transaction management, optimistic/pessimistic locking, double-entry ledger, idempotency, rate limiting, global exception handling, validation, audit log, pagination, Testcontainers, JUnit, Mockito, Docker, GitHub Actions, Actuator, (Prometheus, Grafana, performance testing ở M4)

---

*Tài liệu này bổ sung cho Phase 0 (Requirement Document, ERD, API list chi tiết). Khi bắt đầu Phase 0, nên tách file `PHASE-0-REQUIREMENTS-AND-DESIGN.md` với use case và diagram đầy đủ.*
