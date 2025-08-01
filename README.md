# NANO DC Monitoring Compose

## 📋 개요
NANO DC Monitoring Compose는 안드로이드 Jetpack Compose를 사용하여 구축된 데이터센터 모니터링 애플리케이션입니다. 
여러 기기에서 사용되며, 기기별로 다른 이미지 순서를 설정할 수 있는 확장 가능한 구조로 설계되었습니다.

## ✨ 주요 특징
- 🎯 **기기별 커스터마이징**: 각 기기마다 다른 이미지 순서 설정 가능
- 🔧 **확장 가능한 구조**: 새로운 기기 타입 쉽게 추가 가능
- 🎨 **다양한 레이아웃**: 세로 목록, 가로 스크롤, 그리드 지원
- 🛡️ **타입 안전성**: Kotlin Enum을 통한 컴파일 타임 안전성 보장
- 🔄 **재사용 가능한 컴포넌트**: 클린코드 원칙에 따른 모듈화된 설계
- ⚡ **런타임 변경**: 앱 실행 중에도 설정 변경 가능
- 🔐 **관리자 접근**: LOGO_ZETACUBE 8번 클릭으로 관리자 기능 접근
- 🌐 **Aethir 노드 모니터링**: 전용 Aethir 노드 정보 표시 및 관리

## 🚀 새로운 기능: Aethir 노드 정보

### 3번째 위치 - NODE_INFO_AETHIR
3번째 위치에 배치된 `node_info_aethir` 이미지를 클릭하면 Aethir 네트워크 노드의 상세 정보를 확인할 수 있습니다.

#### 📊 표시되는 정보
**1. 지갑 정보 (Wallet Information)**
- CLAIMABLE - SERVICE FEE: 클레임 가능한 서비스 수수료
- CLAIMABLE - POC & POD REWARDS: POC 및 POD 리워드
- WITHDRAWABLE: 출금 가능한 금액
- VESTING CLAIM: 베스팅 클레임 정보
- VESTING WITHDRAW: 베스팅 출금 정보
- CASH OUT TOTAL: 총 현금화 금액
- STAKED: 스테이킹된 ATH 토큰
- UNSTAKING: 언스테이킹 중인 ATH 토큰
- UNSTAKED: 언스테이킹 완료된 ATH 토큰

**2. 리소스 개요 (Resource Overview)**
- TOTAL LOCATIONS: 총 위치 수
- TOTAL SERVERS: 총 서버 수
- MY AETHIR EARTH: 내 Aethir Earth 자원
- MY AETHIR ATMOSPHERE: 내 Aethir Atmosphere 자원

**3. 일일 수입 정보 (Daily Income)**
- SERVICE FEE: 서비스 수수료 수입
- POC REWARD: POC 리워드 수입
- POD REWARD: POD 리워드 수입
- Total Daily Earnings: 일일 총 수익

#### 🎨 UI 특징
- **색상 구분**: 각 정보 카테고리별로 다른 색상 테마 적용
- **진행 막대**: 베스팅 정보를 시각적으로 표시
- **하이라이트**: 중요한 수치들을 강조 표시
- **카드 레이아웃**: 정보별로 구분된 카드 형태의 깔끔한 UI

#### 💻 구현 특징
- **확장 가능한 구조**: 새로운 Aethir 정보 추가 용이
- **재사용 가능한 컴포넌트**: 다른 노드 타입에도 적용 가능
- **실시간 데이터 지원**: API 연동을 통한 실시간 정보 업데이트 준비
- **글로벌 대응**: 영어 기반 UI로 글로벌 환경 지원

## 🎯 이미지 순서 (기본 설정)
현재 설정된 기본 이미지 순서는 다음과 같습니다:

1. **ndp_info** - NDP 정보
2. **node_info** - 노드 정보  
3. **node_info_aethir** - Aethir 노드 정보 (NEW!)
4. **switch_100g** - 100G 스위치
5. **node_miner** - 노드 마이너
6. **postworker** - 포스트워커
7. **supra** - 수프라
8. **supra_none** (3개) - 수프라 없음
9. **systemtoai** - 시스템투AI
10. **systemtoai_none** - 시스템투AI 없음
11. **aethir** - 에이서
12. **aethir_none**  에이서 없음
13. **filecoin** - 파일코인
14. **filecoin_none** (2개) - 파일코인 없음
15. **not_storage** - 스토리지 없음
16. **upscontroller** - UPS 컨트롤러
17. **logo_zetacube** - 제타큐브 로고

## 🏗️ 프로젝트 구조

```
app/src/main/java/com/nanodatacenter/nanodcmonitoring_compose/
├── data/
│   ├── ImageType.kt              # 이미지 타입 정의 (Enum)
│   └── ImageConfiguration.kt     # 기기별 설정 데이터 클래스
├── manager/
│   ├── ImageOrderManager.kt      # 이미지 순서 관리 (Singleton)
│   └── AdminAccessManager.kt     # 관리자 접근 기능 관리 (Singleton)
├── ui/component/
│   ├── DataCenterComponents.kt      # 데이터센터 모니터링 UI 컴포넌트
│   ├── AethirNodeComponents.kt      # Aethir 노드 전용 UI 컴포넌트 (NEW!)
│   ├── NodeComponents.kt            # 일반 노드 정보 UI 컴포넌트
│   ├── ScoreComponents.kt           # 스코어 표시 UI 컴포넌트
│   └── AdminComponents.kt           # 관리자 접근 UI 컴포넌트
├── util/
│   └── ImageConfigurationHelper.kt   # 설정 생성 헬퍼
├── sample/
│   ├── ImageLayoutExamples.kt        # 레이아웃 예시
│   └── ProductionUsageExamples.kt    # 실제 사용 예시
└── MainActivity.kt               # 메인 액티비티
```

## 🚀 빠른 시작

### 1. 기본 사용법
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 설정 초기화
        ImageConfigurationHelper.applyAllConfigurations()
        val imageOrderManager = ImageOrderManager.getInstance()
        imageOrderManager.setCurrentDeviceType(DeviceType.DEFAULT)
        
        setContent {
            NANODCMonitoring_ComposeTheme {
                // 세로 목록으로 이미지 표시 (기본)
                MonitoringImageList(
                    deviceType = DeviceType.DEFAULT
                )
            }
        }
    }
}
```

### 2. 다양한 레이아웃 사용
```kotlin
// 세로 목록 (기본)
MonitoringImageList(
    deviceType = DeviceType.DEFAULT,
    showDescriptions = false
)

// 가로 스크롤
MonitoringImageRow(
    deviceType = DeviceType.DEFAULT,
    itemWidth = 300
)

// 그리드 (2열)
MonitoringImageGrid(
    deviceType = DeviceType.DEFAULT,
    columns = 2
)
```

### 3. 기기별 다른 순서 적용
```kotlin
// 기기 타입 변경
val manager = ImageOrderManager.getInstance()
manager.setCurrentDeviceType(DeviceType.DEVICE_A)  // 기기 A 순서로 변경
```

## 🔐 관리자 접근 기능

### LOGO_ZETACUBE 클릭 시 관리자 기능
LOGO_ZETACUBE 이미지를 8번 클릭하면 관리자 팝업이 나타납니다.

#### 기능 상세
- **3번 터치 후**: "X번 더 터치하면 관리자 팝업이 나옵니다" 토스트 메시지 표시
- **8번 터치 완료**: 관리자 접근 다이얼로그 표시
- **카운트 리셋**: 관리자 팝업이 표시되면 클릭 카운트 자동 리셋

#### 구현 특징
- **확장 가능한 구조**: `AdminAccessManager`를 통한 중앙화된 관리
- **타입 안전성**: `ImageType.isAdminAccess` 프로퍼티로 관리자 접근 이미지 식별
- **상태 관리**: Compose state를 활용한 반응형 UI
- **사용자 피드백**: 토스트 메시지로 진행 상황 안내

#### 사용 예시
```kotlin
// AdminAccessManager 인스턴스 가져오기
val adminManager = AdminAccessManager.getInstance()

// 현재 클릭 횟수 확인
val currentClicks = adminManager.clickCount

// 수동으로 관리자 기능 초기화 (필요시)
adminManager.reset()

// 디버그 정보 확인
val debugInfo = adminManager.getDebugInfo()
```

#### 확장 가능성
- **관리자 메뉴 추가**: `onAdminAccess` 콜백을 통해 추가 기능 구현 가능
- **다른 이미지 지원**: `ImageType.ADMIN_ACCESS_TYPES`에 새로운 이미지 추가 가능
- **클릭 횟수 조정**: `REQUIRED_CLICKS_FOR_ADMIN` 상수로 필요 클릭 횟수 변경 가능
- **토스트 임계점 조정**: `TOAST_START_THRESHOLD` 상수로 토스트 시작 지점 변경 가능

## 🔧 기기 설정 커스터마이징

### 새로운 기기 타입 추가

1. **DeviceType enum에 새로운 타입 추가**
```kotlin
enum class DeviceType(val displayName: String) {
    DEFAULT("기본"),
    DEVICE_A("기기 A"),
    DEVICE_B("기기 B"),
    NEW_DEVICE("새로운 기기")  // 새로 추가
}
```

2. **ImageConfigurationHelper에 설정 메서드 추가**
```kotlin
fun createNewDeviceConfiguration(): ImageConfiguration {
    val customOrder = listOf(
        ImageType.LOGO_ZETACUBE,
        ImageType.NDP_INFO,
        ImageType.UPS_CONTROLLER,
        // ... 원하는 순서대로 배치
    )
    return ImageConfiguration(DeviceType.NEW_DEVICE, customOrder)
}
```

3. **applyAllConfigurations()에 추가**
```kotlin
fun applyAllConfigurations() {
    val manager = ImageOrderManager.getInstance()
    manager.addConfiguration(createDeviceAConfiguration())
    manager.addConfiguration(createDeviceBConfiguration())
    manager.addConfiguration(createNewDeviceConfiguration())  // 추가
}
```

### 동적 순서 변경
```kotlin
// 런타임에 순서 변경
val newOrder = listOf(
    ImageType.UPS_CONTROLLER,
    ImageType.LOGO_ZETACUBE,
    ImageType.SWITCH_100G
)

ImageConfigurationHelper.updateOrderForDevice(DeviceType.DEFAULT, newOrder)
```

### 헬퍼 함수 활용
```kotlin
// 특정 이미지들을 우선순위로 설정
val priorityConfig = ImageConfigurationHelper.createConfigurationWithPriority(
    deviceType = DeviceType.DEVICE_A,
    priorityImages = listOf(ImageType.UPS_CONTROLLER, ImageType.SWITCH_100G)
)

// 특정 이미지들 제외
val excludeConfig = ImageConfigurationHelper.createConfigurationWithExclusions(
    deviceType = DeviceType.DEVICE_B,
    excludeImages = listOf(ImageType.FILECOIN, ImageType.FILECOIN_NONE_1)
)

// 완전 커스텀 순서
val customConfig = ImageConfigurationHelper.createCustomConfiguration(
    deviceType = DeviceType.DEVICE_C,
    imageOrder = listOf(ImageType.LOGO_ZETACUBE, ImageType.NDP_INFO)
)
```

## 🎨 레이아웃 옵션

### 세로 목록 (MonitoringImageList)
- 전체 화면을 채우는 기본 레이아웃
- 스크롤 가능한 세로 목록
- 이미지만 표시 또는 설명 포함 옵션

### 가로 스크롤 (MonitoringImageRow)
- 수평 스크롤이 가능한 이미지 목록
- 화면 상단이나 특정 섹션에서 사용
- 아이템 너비 커스터마이징 가능

### 그리드 (MonitoringImageGrid)
- 격자 형태로 이미지 배치
- 열 개수 조정 가능
- 제한된 공간에서 효율적

## 🔍 디버깅 및 확인

### 현재 설정 정보 확인
```kotlin
// 현재 순서 출력
val orderInfo = ImageConfigurationHelper.printCurrentOrder(DeviceType.DEFAULT)
println(orderInfo)

// 지원되는 기기 타입 확인
val manager = ImageOrderManager.getInstance()
val supportedDevices = manager.getSupportedDeviceTypes()
val imageCount = manager.getTotalImageCount(DeviceType.DEFAULT)
```

### 설정 리셋
```kotlin
// 모든 설정을 기본값으로 리셋
ImageConfigurationHelper.resetAllToDefault()
```

## 📁 샘플 코드

프로젝트의 `sample` 패키지에는 다음과 같은 예시들이 포함되어 있습니다:

- **ImageLayoutExamples.kt**: 다양한 레이아웃 옵션들의 사용법
- **ProductionUsageExamples.kt**: 실제 프로덕션 환경에서의 사용 시나리오

## 🛠️ 개발 가이드

### 새로운 이미지 추가
1. `res/drawable/`에 이미지 파일 추가
2. `ImageType` enum에 새로운 항목 추가
3. 필요한 기기별 설정에 해당 이미지 추가

### 새로운 레이아웃 추가
1. `MonitoringImageComponents.kt`에 새로운 Composable 함수 생성
2. 기존 `MonitoringImageItem` 재사용
3. 적절한 Layout Composable 사용 (LazyColumn, LazyRow 등)

## 🎯 확장성 및 유지보수

이 시스템은 다음과 같은 확장성을 제공합니다:

- **기기 타입별 설정**: 각 기기마다 완전히 다른 이미지 순서 가능
- **런타임 변경**: 앱 실행 중에도 설정 변경 가능
- **코드 재사용**: 기존 컴포넌트를 재사용하여 새로운 레이아웃 생성
- **타입 안전성**: 컴파일 타임에 오류 검출
- **설정 관리**: 중앙화된 설정 관리로 일관성 보장

각 기기의 요구사항에 맞게 이미지 순서를 쉽게 조정할 수 있으며, 코드 변경 없이 설정만으로 순서를 변경할 수 있습니다.
