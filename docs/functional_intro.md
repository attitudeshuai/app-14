# 邻里宠物互助寄养平台 - 功能说明文档

## 1. 业务背景与解决的问题

### 1.1 业务背景
随着城市化进程加快，越来越多的家庭养起了宠物，猫咪、狗狗、甚至荷兰猪、兔子等小动物成为人们生活中不可或缺的伙伴。

然而，宠物主人经常面临以下难题：
- **短期出差/旅行**：无法随身携带宠物，送宠物店寄养费用高昂
- **商业化寄养的问题**：宠物店环境嘈杂、人员陌生，宠物容易产生应激反应
- **亲友寄养的不便**：打扰亲友生活，亲友可能不熟悉宠物习性
- **信息不对称**：不知道同小区是否有爱宠邻居可以互助

### 1.2 平台定位
**PetFoster** 是一个专注于**邻里之间**的宠物互助寄养平台，核心是"同小区信任 + 互助 + 评价体系"，用社区的力量解决短期宠物寄养问题。

---

## 2. 用户角色与核心用例

### 2.1 用户角色

| 角色 | 说明 |
|------|------|
| **宠物主人 (Owner)** | 发布寄养需求的用户，拥有宠物 |
| **寄养人 (Fosterer)** | 愿意帮忙照顾邻居宠物的用户 |
| **平台用户** | 所有注册用户，同时可以是主人也可以是寄养人 |

### 2.2 核心用例

#### 用例1：用户注册与认证
```
用户 → 注册账号(用户名/邮箱/密码) → 系统创建账户
用户 → 登录账号 → 系统返回JWT Token
用户 → 使用Token访问受保护接口
```

#### 用例2：发布宠物信息
```
用户 → 录入宠物信息(名字/种类/品种/年龄/饮食/医疗记录/照片)
    → 系统保存宠物档案
    → 该宠物与用户绑定(OwnerId)
```

#### 用例3：发布寄养申请
```
宠物主人 → 选择宠物 + 填写寄养时间 + 备注
         → 指定寄养人(可选)
         → 系统创建寄养申请(状态: Pending)
```

#### 用例4：寄养申请状态流转
```
Pending(待确认) → 双方确认 → Approved(已批准)
               → 一方拒绝/取消 → Cancelled(已取消)

Approved(已批准) → 开始寄养 → InProgress(进行中)
               → 取消 → Cancelled(已取消)

InProgress(进行中) → 寄养结束 → Completed(已完成)
                 → 突发取消 → Cancelled(已取消)

Completed / Cancelled → 终态，不可再变更
```

#### 用例5：寄养日报打卡
```
寄养人 → 每日填写日报(饮食情况/心情/照片/备注)
      → 系统保存日报记录
      → 宠物主人可查看所有日报
```

#### 用例6：寄养完成后评价
```
主人/寄养人 → 对对方评分(1-5星) + 文字评价
           → 系统保存评价
           → 参与用户信誉计算
```

---

## 3. 功能模块详细说明

### 3.1 用户认证模块
- **注册**：用户名(唯一) + 邮箱(唯一) + 密码，密码使用 BCrypt 加密存储
- **登录**：用户名 + 密码验证，成功后返回 JWT Token（有效期24小时）
- **个人资料**：查看/更新邮箱、头像，修改密码需验证原密码
- **安全**：所有写操作接口必须携带有效的 Bearer Token

### 3.2 宠物管理模块
- **CRUD**：完整的增删改查，支持分页、按名称搜索、按物种筛选
- **权限**：仅宠物所有者可修改/删除自己的宠物
- **关联**：宠物记录中包含完整的饮食注意事项和医疗记录

### 3.3 寄养申请管理模块
- **创建**：指定宠物、起止时间、寄养人(可选)、每日护理备注
- **搜索**：按状态、主人、寄养人、宠物多维度筛选
- **状态流转**：严格的状态机校验，防止非法状态变更
- **权限**：主人/寄养人均可修改状态，仅主人可删除

### 3.4 寄养日报管理模块
- **日报创建**：按日期记录，同一天同一申请仅能有一份日报
- **内容**：饮食、心情、照片(URL)、文字备注
- **查看**：支持按申请ID、寄养人、日期范围筛选

### 3.5 评价管理模块
- **双向评价**：寄养完成后双方均可评价
- **评分**：1-5星整数评分
- **信誉**：用户评分 = 所有被评价的平均分

### 3.6 统计与搜索模块
- **总览统计**：用户数、宠物数、申请数、完成数、今日新增
- **分布统计**：申请状态分布、宠物种类分布
- **排行榜**：按评分排序的优质用户Top N
- **趋势统计**：按日统计的新增申请、完成申请、新增用户、新增评价

---

## 4. 数据库 ER 图文字描述

### 4.1 表关系概览

```
Users (用户表)
  ├── 1:N → Pets (ownerId): 一个用户拥有多只宠物
  ├── 1:N → FosterRequests as Owner (ownerId): 作为主人发布多个寄养申请
  └── 1:N → FosterRequests as Fosterer (fostererId): 作为寄养人承接多个寄养申请

Pets (宠物表)
  └── 1:N → FosterRequests (petId): 一只宠物可被多次寄养

FosterRequests (寄养申请表)
  ├── 1:N → FosterDailyLogs (requestId): 一次寄养产生多条日报
  └── 1:N → FosterReviews (requestId): 一次寄养最多产生两条评价(双方互评)

FosterReviews (寄养评价表)
  ├── N:1 → Users as Reviewer (reviewerId): 评价人
  └── N:1 → Users as Reviewee (revieweeId): 被评价人
```

### 4.2 各表字段说明

#### Users 用户表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| Id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| Username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| Email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱 |
| PasswordHash | VARCHAR(255) | NOT NULL | BCrypt密码哈希 |
| Avatar | VARCHAR(500) | NULL | 头像URL |
| CreatedAt | DATETIME | NOT NULL | 创建时间 |
| UpdatedAt | DATETIME | NOT NULL | 更新时间 |

#### Pets 宠物表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| Id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| OwnerId | BIGINT | FK → Users.Id, NOT NULL | 宠物主人ID |
| Name | VARCHAR(50) | NOT NULL | 宠物名称 |
| Species | VARCHAR(20) | NOT NULL | 猫/狗/其他 |
| Breed | VARCHAR(50) | NULL | 品种 |
| Age | INT | NULL | 年龄 |
| DietNotes | VARCHAR(1000) | NULL | 饮食注意事项 |
| MedicalNotes | VARCHAR(1000) | NULL | 医疗记录 |
| PhotoUrl | VARCHAR(500) | NULL | 宠物照片URL |
| CreatedAt | DATETIME | NOT NULL | 创建时间 |

#### FosterRequests 寄养申请表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| Id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| PetId | BIGINT | FK → Pets.Id, NOT NULL | 宠物ID |
| OwnerId | BIGINT | FK → Users.Id, NOT NULL | 主人ID |
| FostererId | BIGINT | FK → Users.Id, NULL | 寄养人ID |
| StartDate | DATE | NOT NULL | 寄养开始日期 |
| EndDate | DATE | NOT NULL | 寄养结束日期 |
| DailyCareNotes | VARCHAR(2000) | NULL | 每日护理备注 |
| Status | ENUM | NOT NULL | Pending/Approved/InProgress/Completed/Cancelled |
| CreatedAt | DATETIME | NOT NULL | 创建时间 |

#### FosterDailyLogs 寄养日报表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| Id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| RequestId | BIGINT | FK → FosterRequests.Id, NOT NULL | 寄养申请ID |
| FostererId | BIGINT | FK → Users.Id, NOT NULL | 寄养人ID |
| LogDate | DATE | NOT NULL | 日报日期 |
| Food | VARCHAR(500) | NULL | 饮食情况 |
| Mood | VARCHAR(200) | NULL | 心情/状态 |
| Photos | VARCHAR(2000) | NULL | 照片URL，多个用逗号分隔 |
| Note | VARCHAR(2000) | NULL | 文字备注 |

#### FosterReviews 寄养评价表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| Id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| RequestId | BIGINT | FK → FosterRequests.Id, NOT NULL | 寄养申请ID |
| ReviewerId | BIGINT | FK → Users.Id, NOT NULL | 评价人ID |
| RevieweeId | BIGINT | FK → Users.Id, NOT NULL | 被评价人ID |
| Rating | INT | NOT NULL, 1-5 | 评分 |
| Content | VARCHAR(2000) | NULL | 评价内容 |
| CreatedAt | DATETIME | NOT NULL | 创建时间 |

---

## 5. 关键业务规则

### 5.1 寄养申请状态流转规则

| 当前状态 | 允许变更为 | 操作人 |
|----------|-----------|--------|
| **Pending (待确认)** | Approved, Cancelled | 主人或寄养人 |
| **Approved (已批准)** | InProgress, Cancelled | 主人或寄养人 |
| **InProgress (进行中)** | Completed, Cancelled | 主人或寄养人 |
| **Completed (已完成)** | - (终态) | - |
| **Cancelled (已取消)** | - (终态) | - |

### 5.2 权限规则
- **宠物操作**：仅 `ownerId == 当前用户` 可修改/删除
- **寄养申请修改**：仅 `ownerId == 当前用户` 可编辑基础信息
- **寄养申请状态变更**：`ownerId 或 fostererId == 当前用户` 可操作
- **寄养日报**：仅 `fostererId == 当前用户` 可创建/修改/删除
- **寄养评价**：仅 `reviewerId == 当前用户` 可创建/修改/删除
- **未登录用户**：只读GET接口可访问，所有写操作返回 401

### 5.3 时间校验规则
- 寄养申请的 `EndDate` 必须 ≥ `StartDate`
- 寄养申请的 `EndDate` 不能早于今天（创建时）
- 寄养日报的 `LogDate` 必须在申请的 `[StartDate, EndDate]` 范围内
- 每个申请的每个日期只能有一条日报

### 5.4 评价规则
- 仅 `Status == Completed` 的寄养申请可评价
- 评价人必须是申请的主人或寄养人
- 必须评价对方（主人评价寄养人，寄养人评价主人）
- 每个用户对同一寄养申请只能评价一次

---

## 6. 接口调用示例

### 6.1 用户注册

**请求**：
```http
POST /api/auth/register HTTP/1.1
Host: localhost:8084
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "123456",
  "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=newuser"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 6,
      "username": "newuser",
      "email": "newuser@example.com",
      "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=newuser",
      "createdAt": "2025-06-15 10:30:00"
    }
  }
}
```

### 6.2 创建寄养申请

**请求**：
```http
POST /api/fosterrequests HTTP/1.1
Host: localhost:8084
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "petId": 1,
  "fostererId": 2,
  "startDate": "2025-07-01",
  "endDate": "2025-07-07",
  "dailyCareNotes": "每天早晚各喂一次，记得清理猫砂盆，小白喜欢玩逗猫棒"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 9,
    "petId": 1,
    "petName": "小白",
    "ownerId": 1,
    "ownerUsername": "zhangsan",
    "fostererId": 2,
    "fostererUsername": "lisi",
    "startDate": "2025-07-01",
    "endDate": "2025-07-07",
    "dailyCareNotes": "每天早晚各喂一次，记得清理猫砂盆，小白喜欢玩逗猫棒",
    "status": "Pending",
    "createdAt": "2025-06-15 10:35:00"
  }
}
```

### 6.3 获取总览统计

**请求**：
```http
GET /api/stats/overview HTTP/1.1
Host: localhost:8084
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUsers": 5,
    "totalPets": 8,
    "totalRequests": 8,
    "totalCompletedRequests": 2,
    "totalReviews": 4,
    "todayNewRequests": 0,
    "requestStatusCount": {
      "Pending": 2,
      "Approved": 1,
      "InProgress": 2,
      "Completed": 2,
      "Cancelled": 1
    },
    "petSpeciesCount": {
      "猫": 4,
      "狗": 3,
      "其他": 1
    },
    "topRatedUsers": [
      {
        "userId": 1,
        "username": "zhangsan",
        "averageRating": 5.0,
        "reviewCount": 2
      },
      {
        "userId": 2,
        "username": "lisi",
        "averageRating": 4.5,
        "reviewCount": 2
      }
    ]
  }
}
```
