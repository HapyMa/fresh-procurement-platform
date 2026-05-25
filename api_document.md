# 生鲜采购平台 API 接口文档

> 版本：v1.0  
> 协议：RESTful API  
> 数据格式：JSON  
> 认证方式：JWT Token (Authorization: Bearer {token})

---

## 通用规范

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 状态码

| Code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 一、用户模块

### 1.1 用户注册

```
POST /api/v1/auth/register
```

**请求参数：**

```json
{
  "phone": "13800138000",
  "password": "123456",
  "userType": 1,
  "nickname": "老王餐厅"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 10001,
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expireAt": "2024-12-31T23:59:59"
  }
}
```

### 1.2 用户登录

```
POST /api/v1/auth/login
```

**请求参数：**

```json
{
  "phone": "13800138000",
  "password": "123456"
}
```

### 1.3 获取用户信息

```
GET /api/v1/user/profile
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "userId": 10001,
    "phone": "13800138000",
    "nickname": "老王餐厅",
    "avatarUrl": "https://...",
    "userType": 1,
    "verifyStatus": 2,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### 1.4 提交认证信息

```
POST /api/v1/user/verify
```

**请求参数：**

```json
{
  "verificationType": 1,
  "realName": "王小明",
  "idCardNo": "510123199001011234",
  "businessName": "老王餐厅",
  "businessLicenseNo": "91510100MA6CXXXXXX",
  "businessLicenseUrl": "https://...",
  "foodLicenseUrl": "https://..."
}
```

### 1.5 地址管理

#### 添加地址
```
POST /api/v1/user/addresses
```

```json
{
  "addressType": 1,
  "contactName": "王小明",
  "contactPhone": "13800138000",
  "province": "四川省",
  "city": "成都市",
  "district": "武侯区",
  "detailAddress": "科华北路XX号",
  "longitude": 104.0668,
  "latitude": 30.5728,
  "isDefault": 1
}
```

#### 获取地址列表
```
GET /api/v1/user/addresses?addressType=1
```

#### 删除地址
```
DELETE /api/v1/user/addresses/{addressId}
```

---

## 二、采购商模块

### 2.1 发布采购需求

```
POST /api/v1/buyer/demands
```

**请求参数：**

```json
{
  "categoryId": 7,
  "productName": "土豆",
  "quantity": 200,
  "unit": "斤",
  "maxPrice": 3.5,
  "qualityRequirement": "一级品，无发芽，单个重量150g以上",
  "deliveryAddressId": 1001,
  "deliveryDate": "2024-01-20",
  "deliveryTimeSlot": "上午9-12点",
  "remark": "需要送货到后门"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "demandId": 20001,
    "status": 1,
    "groupId": null,
    "createdAt": "2024-01-15T14:30:00"
  }
}
```

### 2.2 获取我的需求列表

```
GET /api/v1/buyer/demands?page=1&size=20&status=0
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "total": 50,
    "list": [
      {
        "demandId": 20001,
        "productName": "土豆",
        "quantity": 200,
        "unit": "斤",
        "status": 3,
        "statusText": "报价中",
        "groupId": 5001,
        "mergedQuantity": 450,
        "quoteCount": 3,
        "createdAt": "2024-01-15T14:30:00"
      }
    ]
  }
}
```

### 2.3 查看需求详情（含报价列表）

```
GET /api/v1/buyer/demands/{demandId}
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "demandId": 20001,
    "productName": "土豆",
    "quantity": 200,
    "unit": "斤",
    "qualityRequirement": "一级品...",
    "deliveryDate": "2024-01-20",
    "status": 3,
    
    "groupInfo": {
      "groupId": 5001,
      "city": "成都市",
      "totalQuantity": 450,
      "demandCount": 3,
      "status": 2
    },
    
    "quotes": [
      {
        "quoteId": 8001,
        "supplierId": 10002,
        "supplierName": "XX蔬菜批发",
        "supplierScore": 4.8,
        "unitPrice": 2.5,
        "totalAmount": 1125.00,
        "validHours": 24,
        "expireAt": "2024-01-16T14:30:00",
        "remark": "量大从优，品质保证"
      }
    ]
  }
}
```

### 2.4 选择报价

```
POST /api/v1/buyer/demands/{demandId}/select-quote
```

**请求参数：**

```json
{
  "quoteId": 8001
}
```

**响应：**

```json
{
  "code": 200,
  "message": "选择成功，等待供应商发货",
  "data": {
    "demandId": 20001,
    "status": 5,
    "dealPrice": 2.5,
    "dealTotalAmount": 500.00,
    "supplierId": 10002,
    "supplierName": "XX蔬菜批发"
  }
}
```

### 2.5 确认收货

```
POST /api/v1/buyer/demands/{demandId}/confirm-receipt
```

**请求参数：**

```json
{
  "actualWeight": 205.5,
  "remark": "重量略有超出，品质不错"
}
```

### 2.6 评价供应商

```
POST /api/v1/buyer/demands/{demandId}/review
```

```json
{
  "qualityScore": 5,
  "serviceScore": 5,
  "deliveryScore": 4,
  "overallScore": 5,
  "content": "土豆很新鲜，送货准时",
  "images": ["https://...", "https://..."]
}
```

---

## 三、供应商模块

### 3.1 获取可报价的合并需求列表

```
GET /api/v1/supplier/demand-groups?page=1&size=20&city=成都市
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "total": 15,
    "list": [
      {
        "groupId": 5001,
        "categoryId": 7,
        "productName": "土豆",
        "city": "成都市",
        "totalQuantity": 450,
        "unit": "斤",
        "demandCount": 3,
        "mergeDeadline": "2024-01-15T18:00:00",
        "quoteCount": 2,
        "status": 2
      }
    ]
  }
}
```

### 3.2 查看合并需求详情（含子订单列表）

```
GET /api/v1/supplier/demand-groups/{groupId}
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "groupId": 5001,
    "productName": "土豆",
    "city": "成都市",
    "totalQuantity": 450,
    "unit": "斤",
    "status": 2,
    
    "demands": [
      {
        "demandId": 20001,
        "buyerId": 10001,
        "buyerName": "老王餐厅",
        "quantity": 200,
        "maxPrice": 3.5,
        "qualityRequirement": "一级品...",
        "deliveryAddress": {
          "province": "四川省",
          "city": "成都市",
          "district": "武侯区",
          "detail": "科华北路XX号",
          "lng": 104.0668,
          "lat": 30.5728
        },
        "deliveryDate": "2024-01-20",
        "deliveryTimeSlot": "上午9-12点"
      },
      {
        "demandId": 20002,
        "buyerId": 10003,
        "buyerName": "学校食堂",
        "quantity": 150,
        "deliveryAddress": {
          "district": "锦江区",
          "detail": "XX街XX号",
          "lng": 104.0889,
          "lat": 30.6574
        }
      },
      {
        "demandId": 20003,
        "buyerId": 10004,
        "buyerName": "社区团购",
        "quantity": 100,
        "deliveryAddress": {
          "district": "高新区",
          "detail": "XX路XX号",
          "lng": 104.0556,
          "lat": 30.5367
        }
      }
    ]
  }
}
```

### 3.3 提交报价

```
POST /api/v1/supplier/demand-groups/{groupId}/quotes
```

**请求参数：**

```json
{
  "unitPrice": 2.5,
  "validHours": 24,
  "remark": "量大从优，品质保证，成都本地货源"
}
```

### 3.4 获取我的报价列表

```
GET /api/v1/supplier/quotes?page=1&size=20&status=1
```

### 3.5 获取已接订单列表

```
GET /api/v1/supplier/orders?page=1&size=20&status=5
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "total": 10,
    "list": [
      {
        "demandId": 20001,
        "productName": "土豆",
        "quantity": 200,
        "dealPrice": 2.5,
        "dealTotalAmount": 500.00,
        "buyerName": "老王餐厅",
        "deliveryAddress": "成都市武侯区科华北路XX号",
        "deliveryDate": "2024-01-20",
        "packStatus": 0,
        "packStatusText": "待分拣"
      }
    ]
  }
}
```

---

## 四、分拣打包模块（供应商端核心）

### 4.1 获取待分拣订单列表

```
GET /api/v1/supplier/pack/pending-list?page=1&size=20
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "total": 5,
    "list": [
      {
        "demandId": 20001,
        "productName": "土豆",
        "plannedQuantity": 200,
        "unit": "斤",
        "buyerName": "老王餐厅",
        "deliveryAddress": "成都市武侯区...",
        "packStatus": 0
      }
    ]
  }
}
```

### 4.2 开始分拣

```
POST /api/v1/supplier/pack/{demandId}/start
```

**响应：**

```json
{
  "code": 200,
  "message": "开始分拣",
  "data": {
    "packRecordId": 9001,
    "demandId": 20001,
    "status": 1,
    "startedAt": "2024-01-18T08:30:00"
  }
}
```

### 4.3 完成分拣并打包

```
POST /api/v1/supplier/pack/{demandId}/complete
```

**请求参数（multipart/form-data）：**

```
actualQuantity: 200          // 实际分拣数量
actualWeight: 102.5         // 实际称重(kg)
grade: "一级"                // 实际等级
qualityCheck: 1             // 质检结果 1-合格
packageCount: 2             // 包裹数量
packageType: "泡沫箱"        // 包装类型
labelCode: PKG202401180001  // 标签码
photos: [File]              // 打包照片（可多选）
remark: "已仔细分拣，品质优良"
```

**响应：**

```json
{
  "code": 200,
  "message": "打包完成",
  "data": {
    "packRecordId": 9001,
    "demandId": 20001,
    "status": 2,
    "packages": [
      {
        "packageId": 10001,
        "packageNo": "PKG202401180001-1",
        "labelCode": "PKG202401180001-1",
        "weight": 52.5
      },
      {
        "packageId": 10002,
        "packageNo": "PKG202401180001-2",
        "labelCode": "PKG202401180001-2",
        "weight": 50.0
      }
    ],
    "completedAt": "2024-01-18T09:15:00"
  }
}
```

### 4.4 获取打包记录详情

```
GET /api/v1/supplier/pack/{demandId}/record
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "packRecordId": 9001,
    "demandId": 20001,
    "plannedQuantity": 200,
    "actualQuantity": 200,
    "actualWeight": 102.5,
    "weightDeviation": 2.5,
    "grade": "一级",
    "qualityCheck": 1,
    "packageCount": 2,
    "packageType": "泡沫箱",
    "labelCode": "PKG202401180001",
    "photos": ["https://...", "https://..."],
    "remark": "已仔细分拣",
    "status": 2,
    "packedAt": "2024-01-18T09:15:00"
  }
}
```

### 4.5 发货

```
POST /api/v1/supplier/ship/{demandId}
```

**请求参数：**

```json
{
  "packageIds": [10001, 10002],
  "logisticsType": 1,
  "logisticsCompany": "顺丰速运",
  "trackingNo": "SF1234567890",
  "estimatedArrival": "2024-01-20T10:00:00"
}
```

**logisticsType 说明：**
- 1: 第三方物流
- 2: 供应商自配送
- 3: 采购商自提

### 4.6 生成配送路线

```
POST /api/v1/supplier/routes/generate
```

**请求参数：**

```json
{
  "routeDate": "2024-01-20",
  "demandIds": [20001, 20002, 20003]
}
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "routeId": 6001,
    "routeDate": "2024-01-20",
    "stopCount": 3,
    "totalDistance": 28.5,
    "estimatedDuration": 90,
    "routePoints": [
      {
        "sequence": 1,
        "demandId": 20001,
        "buyerName": "老王餐厅",
        "address": "武侯区科华北路XX号",
        "lng": 104.0668,
        "lat": 30.5728,
        "estimatedArrival": "09:30"
      },
      {
        "sequence": 2,
        "demandId": 20002,
        "buyerName": "学校食堂",
        "address": "锦江区XX街XX号",
        "lng": 104.0889,
        "lat": 30.6574,
        "estimatedArrival": "10:15"
      },
      {
        "sequence": 3,
        "demandId": 20003,
        "buyerName": "社区团购",
        "address": "高新区XX路XX号",
        "lng": 104.0556,
        "lat": 30.5367,
        "estimatedArrival": "11:00"
      }
    ]
  }
}
```

---

## 五、商品库模块（供应商）

### 5.1 添加商品

```
POST /api/v1/supplier/products
```

```json
{
  "categoryId": 7,
  "name": "土豆",
  "spec": "单个150g以上",
  "origin": "四川德阳",
  "grade": "一级",
  "mainImage": "https://...",
  "description": "本地新鲜土豆...",
  "minOrderQuantity": 100,
  "supplyCities": ["成都市", "德阳市"]
}
```

### 5.2 获取商品列表

```
GET /api/v1/supplier/products?page=1&size=20&status=1
```

### 5.3 更新商品

```
PUT /api/v1/supplier/products/{productId}
```

### 5.4 上下架商品

```
PATCH /api/v1/supplier/products/{productId}/status
```

```json
{
  "status": 0
}
```

---

## 六、消息通知模块

### 6.1 获取消息列表

```
GET /api/v1/messages?page=1&size=20&isRead=0
```

### 6.2 标记消息已读

```
PATCH /api/v1/messages/{msgId}/read
```

### 6.3 删除消息

```
DELETE /api/v1/messages/{msgId}
```

---

## 七、公共模块

### 7.1 获取商品分类列表

```
GET /api/v1/categories?parentId=0
```

### 7.2 上传图片

```
POST /api/v1/upload/image
```

**请求：** multipart/form-data，file字段

**响应：**

```json
{
  "code": 200,
  "data": {
    "url": "https://cdn.example.com/images/xxx.jpg"
  }
}
```

### 7.3 获取城市列表

```
GET /api/v1/cities
```

---

## WebSocket 实时通知

连接地址：`wss://api.example.com/ws?token={jwt_token}`

### 消息类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `quote.new` | 新报价 | 您的需求收到新报价 |
| `quote.selected` | 报价被选中 | 您的报价被采购商选中 |
| `order.shipped` | 订单发货 | 您的订单已发货 |
| `order.delivered` | 订单签收 | 采购商已确认收货 |
| `group.merged` | 需求被合并 | 您的需求已加入合并组 |

### 消息格式

```json
{
  "type": "quote.new",
  "timestamp": "2024-01-15T14:35:00",
  "data": {
    "demandId": 20001,
    "quoteId": 8001,
    "supplierName": "XX蔬菜批发",
    "unitPrice": 2.5
  }
}
```
