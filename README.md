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

## 🎯 이미지 순서 (기본 설정)
현재 설정된 기본 이미지 순서는 다음과 같습니다:

1. **ndp_info** - NDP 정보
2. **node_info** - 노드 정보  
3. **onboarding** - 온보딩
4. **switch_100g** - 100G 스위치
5. **node_miner** - 노드 마이너
6. **postworker** - 포스트워커
7. **supra** - 수프라
8. **supra_none** (3개) - 수프라 없음
9. **deepseek** - 딥시크
10. **deepseek_none** - 딥시크 없음
11. **aethir** - 에테르
12. **aethir_none** - 에테르 없음
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
│   └── ImageOrderManager.kt      # 이미지 순서 관리 (Singleton)
├── ui/component/
│   └── MonitoringImageComponents.kt  # 재사용 가능한 UI 컴포넌트
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
