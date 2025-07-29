# 원본 크기 이미지 표시 가이드

## 개요
이 가이드는 NANO DC Monitoring Compose 앱에서 이미지를 원본 크기로 표시하는 방법에 대해 설명합니다. 다양한 디바이스에서 확장성을 고려한 클린 코드 구조로 구현되었습니다.

## 주요 변경사항

### 1. 새로운 유틸리티 클래스 추가
- `ImageScaleUtil`: 이미지 스케일링 모드 관리
- 6가지 스케일링 옵션 제공
- 디바이스별 권장 스케일링 모드 자동 선택

### 2. 기존 컴포넌트 개선
- `MonitoringImageItem`: 원본 크기 표시 지원 추가
- `MonitoringImageList`: 스케일링 모드 선택 가능
- `MonitoringImageRow`: 가로 스크롤에서 원본 크기 지원
- `MonitoringImageGrid`: 그리드에서 원본 크기 지원

### 3. 새로운 컴포넌트 추가
- `OriginalSizeImageViewer`: 단일 이미지 원본 크기 뷰어
- `OriginalSizeImageList`: 여러 이미지 원본 크기 리스트
- `OriginalSizeDataCenterScreen`: 전체 화면 원본 크기 모니터링

## 사용법

### 기본 원본 크기 표시
```kotlin
MonitoringImageList(
    deviceType = DeviceType.DEFAULT,
    scaleMode = ImageScaleUtil.ScaleMode.ORIGINAL,
    useFixedHeight = false,
    showDescriptions = true
)
```

### 완전한 원본 크기 뷰어
```kotlin
OriginalSizeImageList(
    deviceType = DeviceType.DEFAULT,
    showDescriptions = true,
    onImageClick = { imageType ->
        // 클릭 처리 로직
    }
)
```

### 전체 화면 원본 크기 모니터링
```kotlin
OriginalSizeDataCenterScreen(
    deviceType = DeviceType.DEFAULT
)
```

### 스케일링 모드 선택
```kotlin
MonitoringImageItem(
    imageType = imageType,
    scaleMode = ImageScaleUtil.ScaleMode.ORIGINAL, // 또는 다른 모드
    useFixedHeight = false
)
```

## 스케일링 모드 옵션

| 모드 | 설명 | 사용 시기 |
|------|------|-----------|
| `ORIGINAL` | 원본 크기 유지 (스크롤 가능) | 정확한 이미지 확인 필요시 |
| `FIT_ASPECT_RATIO` | 비율 유지하며 컨테이너에 맞춤 | 일반적인 표시 |
| `FIT_WIDTH` | 폭에 맞춰 조정 | 세로 스크롤 환경 |
| `FIT_HEIGHT` | 높이에 맞춰 조정 | 가로 레이아웃 |
| `FILL_BOUNDS` | 컨테이너 완전 채움 | 배경 이미지 |
| `INSIDE` | 클 때만 축소 | 작은 이미지 보호 |

## 디바이스별 최적화

### 태블릿 (가로 모드)
```kotlin
val isTablet = configuration.screenWidthDp >= 600
val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

if (isTablet && isLandscape) {
    // 원본 크기 권장
    scaleMode = ImageScaleUtil.ScaleMode.ORIGINAL
}
```

### 모바일 (세로 모드)
```kotlin
if (!isTablet && !isLandscape) {
    // 폭 맞춤 권장
    scaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH
}
```

## 성능 최적화

### 1. 메모리 관리
- 큰 이미지는 `LazyColumn`/`LazyRow` 사용
- 뷰포트 밖 이미지는 자동으로 메모리에서 해제

### 2. 스크롤 성능
- 원본 크기 이미지는 개별 스크롤 컨테이너 사용
- 중첩 스크롤 방지를 위한 적절한 Modifier 설정

### 3. 렌더링 최적화
- `wrapContentSize()` 사용으로 불필요한 측정 방지
- `ContentScale.None`으로 불필요한 스케일링 연산 제거

## 확장성 고려사항

### 1. 재사용 가능한 컴포넌트
- 모든 컴포넌트는 매개변수화되어 재사용 가능
- 기본값 설정으로 간편한 사용

### 2. 테마 시스템 통합
- Material3 디자인 시스템 준수
- 다크/라이트 모드 지원

### 3. 접근성 지원
- `contentDescription` 필수 설정
- 스크린 리더 지원

## 마이그레이션 가이드

### 기존 코드에서 원본 크기 적용
```kotlin
// 기존
MonitoringImageItem(
    imageType = imageType,
    contentScale = ContentScale.Fit
)

// 변경 후
MonitoringImageItem(
    imageType = imageType,
    scaleMode = ImageScaleUtil.ScaleMode.ORIGINAL,
    useFixedHeight = false
)
```

### 호환성 유지
- 기존 매개변수는 deprecated 처리
- 새로운 매개변수에 적절한 기본값 설정
- 점진적 마이그레이션 가능

## 샘플 코드

`OriginalImageSamples.kt` 파일에서 다양한 사용 예시를 확인할 수 있습니다:

1. `OriginalImageListSample`: 기본 원본 크기 표시
2. `FullOriginalImageListSample`: 완전한 원본 크기 뷰어
3. `ScaleModeSelectableSample`: 스케일링 모드 선택 UI
4. `HybridModeSample`: 일반 뷰와 원본 크기 뷰 전환

## 문제 해결

### 메모리 부족 오류
- 큰 이미지가 많을 때 `LazyColumn` 사용
- 이미지 품질 최적화 고려

### 스크롤 성능 저하
- 중첩 스크롤 제거
- `heightIn(max = ...)` 사용으로 최대 높이 제한

### 레이아웃 깨짐
- `wrapContentSize()` 적절히 사용
- 컨테이너 크기 제한 설정

## 향후 개선 계획

1. 줌/팬 제스처 지원
2. 이미지 캐싱 최적화
3. Progressive loading 구현
4. 고해상도 이미지 지원 강화

---

이 가이드를 통해 NANO DC Monitoring 앱에서 이미지를 원본 크기로 효과적으로 표시할 수 있습니다. 추가 질문이나 개선 사항이 있으면 개발팀에 문의해 주세요.
