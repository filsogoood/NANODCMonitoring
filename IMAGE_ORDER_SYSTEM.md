# NANO DC Monitoring Image Order System

## 개요
이 시스템은 NANO DC 모니터링 애플리케이션에서 이미지 순서를 유연하게 관리할 수 있도록 설계되었습니다. 
여러 기기에서 다른 이미지 순서가 필요한 경우 쉽게 설정을 변경할 수 있습니다.

## 주요 특징
- ✅ 기기별 이미지 순서 커스터마이징
- ✅ 확장 가능한 구조 (새로운 기기 타입 추가 용이)
- ✅ 클린코드 원칙에 따른 재사용 가능한 컴포넌트
- ✅ 타입 안전성 보장 (Enum 사용)
- ✅ 싱글톤 패턴으로 일관된 설정 관리
- ✅ 이미지 전체 표시 (텍스트 설명 없이 순수 이미지만)

## 프로젝트 구조

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
└── MainActivity.kt               # 메인 액티비티
```

## 기본 이미지 순서

현재 설정된 기본 이미지 순서:
1. `ndp_info` - NDP 정보
2. `node_info` - 노드 정보
3. `onboarding` - 온보딩
4. `switch_100g` - 100G 스위치
5. `node_miner` - 노드 마이너
6. `postworker` - 포스트워커
7. `supra` - 수프라
8. `supra_none` - 수프라 없음 (3개)
9. `deepseek` - 딥시크
10. `deepseek_none` - 딥시크 없음
11. `aethir` - 에테르
12. `aethir_none` - 에테르 없음
13. `filecoin` - 파일코인
14. `filecoin_none` - 파일코인 없음 (2개)
15. `not_storage` - 스토리지 없음
16. `upscontroller` - UPS 컨트롤러
17. `logo_zetacube` - 제타큐브 로고

## 사용법

### 1. 기본 사용법

```kotlin
// MainActivity.kt에서
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 이미지 순서 관리자 초기화
        val imageOrderManager = ImageOrderManager.getInstance()
        imageOrderManager.setCurrentDeviceType(DeviceType.DEFAULT)
        
        setContent {
            NANODCMonitoring_ComposeTheme {
                Scaffold { innerPadding ->
                    // 세로 목록으로 이미지 표시
                    MonitoringImageList(
                        deviceType = DeviceType.DEFAULT,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
```

### 2. 다양한 레이아웃 사용

```kotlin
// 세로 목록 (기본: 이미지만 표시)
MonitoringImageList(
    deviceType = DeviceType.DEFAULT
)

// 가로 스크롤 (기본: 이미지만 표시)
MonitoringImageRow(
    deviceType = DeviceType.DEFAULT,
    itemWidth = 300
)

// 그리드 레이아웃 (기본: 이미지만 표시)
MonitoringImageGrid(
    deviceType = DeviceType.DEFAULT,
    columns = 2
)

// 텍스트 설명과 함께 표시하려면 (선택사항)
MonitoringImageList(
    deviceType = DeviceType.DEFAULT,
    showDescriptions = true
)
```

### 3. 기기별 다른 순서 설정

```kotlin
// 커스텀 설정 적용
ImageConfigurationHelper.applyAllConfigurations()

// 특정 기기로 변경
val manager = ImageOrderManager.getInstance()
manager.setCurrentDeviceType(DeviceType.DEVICE_A)  // 기기 A 순서로 변경
```

### 4. 새로운 기기 타입 추가

```kotlin
// 1. DeviceType enum에 새로운 타입 추가
enum class DeviceType(val displayName: String) {
    DEFAULT("기본"),
    DEVICE_A("기기 A"),
    DEVICE_B("기기 B"),
    NEW_DEVICE("새로운 기기")  // 새로 추가
}

// 2. ImageConfigurationHelper에 설정 메서드 추가
fun createNewDeviceConfiguration(): ImageConfiguration {
    val customOrder = listOf(
        ImageType.LOGO_ZETACUBE,
        ImageType.NDP_INFO,
        // ... 원하는 순서대로 배치
    )
    return ImageConfiguration(DeviceType.NEW_DEVICE, customOrder)
}

// 3. applyAllConfigurations()에 추가
fun applyAllConfigurations() {
    val manager = ImageOrderManager.getInstance()
    manager.addConfiguration(createDeviceAConfiguration())
    manager.addConfiguration(createDeviceBConfiguration())
    manager.addConfiguration(createNewDeviceConfiguration())  // 추가
}
```

### 5. 동적 순서 변경

```kotlin
// 런타임에 순서 변경
val newOrder = listOf(
    ImageType.UPS_CONTROLLER,
    ImageType.LOGO_ZETACUBE,
    ImageType.SWITCH_100G
    // ... 원하는 순서
)

ImageConfigurationHelper.updateOrderForDevice(DeviceType.DEFAULT, newOrder)
```

### 6. 디버깅 및 확인

```kotlin
// 현재 순서 확인
val orderInfo = ImageConfigurationHelper.printCurrentOrder(DeviceType.DEFAULT)
println(orderInfo)

// 지원되는 모든 기기 타입 확인
val manager = ImageOrderManager.getInstance()
val supportedDevices = manager.getSupportedDeviceTypes()
```

## 주요 클래스 설명

### ImageType (Enum)
- 모든 이미지 리소스를 타입 안전하게 관리
- drawable 리소스와 설명을 포함
- 추가 이미지가 필요할 때 enum에 새로운 항목 추가

### ImageOrderManager (Singleton)
- 이미지 순서 설정을 중앙에서 관리
- 기기별 다른 설정 지원
- 스레드 안전 보장

### MonitoringImageComponents
- 재사용 가능한 Composable 함수들
- 다양한 레이아웃 지원 (List, Row, Grid)
- 커스터마이징 가능한 옵션들

## 확장 가이드

### 새로운 이미지 추가
1. `res/drawable/`에 이미지 파일 추가
2. `ImageType` enum에 새로운 항목 추가
3. 필요한 기기별 설정에 해당 이미지 추가

### 새로운 레이아웃 추가
1. `MonitoringImageComponents.kt`에 새로운 Composable 함수 생성
2. 기존 `MonitoringImageItem` 재사용
3. 적절한 Layout Composable 사용 (LazyColumn, LazyRow 등)

이 시스템을 통해 각 기기의 요구사항에 맞게 이미지 순서를 쉽게 조정할 수 있으며, 
코드 변경 없이 설정만으로 순서를 변경할 수 있습니다.
