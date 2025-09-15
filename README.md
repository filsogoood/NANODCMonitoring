# NANO DC Monitoring System

## Project Overview

NANO DC Monitoring System is a comprehensive real-time monitoring solution designed for multi-site data center infrastructure management. Built as an Android application using Jetpack Compose, it provides continuous visibility into the operational status of various server nodes, storage systems, and computational resources across multiple data center locations.

## Core Purpose

This system serves as a centralized monitoring dashboard for NANO data centers, enabling administrators and operators to:

- Track real-time performance metrics of distributed server infrastructure
- Monitor cryptocurrency mining operations (Filecoin, Aethir)
- Observe storage system health and utilization
- Manage GPU computing resources and AI workloads
- Oversee network equipment and power systems

## Architecture Philosophy

The application is architected with enterprise-grade scalability and maintainability in mind:

### Multi-Site Support
The system is designed to manage multiple data center locations (GY01, BC01, BC02) from a single interface, with each location having its unique configuration and monitoring requirements.

### Device Adaptability
Recognizing deployment across various Android devices with different screen sizes and specifications, the system implements a flexible configuration management system that allows per-device UI customization without code modification.

### Real-Time Data Pipeline
Utilizing a robust API integration layer, the system maintains a continuous data stream with 20-second refresh intervals, ensuring operators have near real-time visibility into system states.

### Modular Component Design
Following clean code principles, the application separates concerns into distinct modules:
- **Network Layer**: Handles API communication and data synchronization
- **Repository Pattern**: Manages data flow and caching strategies
- **UI Components**: Reusable, composable interface elements
- **Configuration Management**: Dynamic device and data center settings

## Key Monitoring Capabilities

### Server Node Monitoring
- **Filecoin Mining Nodes**: Tracks mining performance, resource utilization, and operational status
- **GPU Worker Nodes**: Monitors computational workloads, temperature, and processing efficiency
- **Post Worker Systems**: Observes proof-of-spacetime operations and verification processes

### Storage Infrastructure
- **NAS Systems**: Displays storage capacity, usage patterns, and health indicators
- **SSD/HDD Arrays**: Monitors disk performance, IOPS, and wear levels
- **Distributed Storage**: Tracks replication status and data integrity

### Network and Power Systems
- **100G Network Switches**: Monitors network throughput and connectivity
- **UPS Controllers**: Tracks power redundancy and battery status
- **Environmental Sensors**: Observes temperature, humidity, and cooling efficiency

### Specialized Workloads
- **Aethir Network Nodes**: Displays earnings, staking status, and network participation
- **AI Computing Resources**: Monitors deep learning workloads and model training
- **Blockchain Operations**: Tracks various blockchain network participations

## Technical Implementation

### Data Visualization
The system employs sophisticated visualization techniques to present complex data in an intuitive format:
- **Hexagonal Performance Charts**: Provides multi-dimensional performance scoring
- **Resource Utilization Graphs**: Shows CPU, memory, and storage usage trends
- **Sector-Based Layouts**: Groups related nodes for logical organization
- **Interactive Expandable Cards**: Allows drilling down into detailed metrics

### Adaptive UI System
The interface dynamically adjusts based on:
- Device characteristics and screen dimensions
- Data center specific requirements
- Node types and their unique monitoring needs
- User role and access levels

### Performance Optimization
Built with efficiency in mind for continuous 24/7 operation:
- Memory-efficient image loading and caching
- Optimized network calls with intelligent batching
- Coroutine-based asynchronous operations
- Hardware acceleration for smooth animations

## Security and Access Control

### Administrator Features
The system includes a hidden administrator interface accessible through a specific interaction pattern, providing:
- Data center switching capabilities
- Configuration management
- Debug information access
- System diagnostics

### Data Protection
- Secure API communication
- Configuration persistence with encryption
- Role-based access control preparation

## Deployment Scenarios

The system is designed for deployment in various operational contexts:

### Control Room Displays
Large-screen tablets mounted in NOC (Network Operations Center) environments for continuous monitoring

### Mobile Supervision
Portable devices for on-site technicians and administrators requiring mobility

### Multi-Screen Setups
Synchronized displays across multiple devices for comprehensive coverage

## Global Readiness

As a global project, the system maintains:
- English-based UI for international operators
- UTC time synchronization for multi-timezone operations
- Standardized metric units
- Culturally neutral iconography

## Future Extensibility

The architecture supports future enhancements including:
- Additional data center locations
- New node types and monitoring categories
- Advanced analytics and predictive maintenance
- Integration with external monitoring systems
- Historical data analysis and reporting

## System Requirements

- Android 7.1.1 (API 25) or higher
- Minimum 2GB RAM
- Stable network connectivity
- Landscape orientation support

---

# NANO DC 모니터링 시스템

## 프로젝트 개요

NANO DC 모니터링 시스템은 다중 사이트 데이터센터 인프라 관리를 위해 설계된 종합적인 실시간 모니터링 솔루션입니다. Jetpack Compose를 사용한 안드로이드 애플리케이션으로 구축되어, 여러 데이터센터 위치에 걸친 다양한 서버 노드, 스토리지 시스템, 컴퓨팅 리소스의 운영 상태를 지속적으로 파악할 수 있도록 합니다.

## 핵심 목적

이 시스템은 NANO 데이터센터를 위한 중앙집중식 모니터링 대시보드 역할을 하며, 관리자와 운영자가 다음을 수행할 수 있도록 합니다:

- 분산 서버 인프라의 실시간 성능 지표 추적
- 암호화폐 마이닝 운영 모니터링 (Filecoin, Aethir)
- 스토리지 시스템 상태 및 활용도 관찰
- GPU 컴퓨팅 리소스 및 AI 워크로드 관리
- 네트워크 장비 및 전력 시스템 감독

## 아키텍처 철학

이 애플리케이션은 엔터프라이즈급 확장성과 유지보수성을 염두에 두고 설계되었습니다:

### 다중 사이트 지원
시스템은 단일 인터페이스에서 여러 데이터센터 위치(GY01, BC01, BC02)를 관리할 수 있도록 설계되었으며, 각 위치는 고유한 구성과 모니터링 요구사항을 가지고 있습니다.

### 기기 적응성
다양한 화면 크기와 사양을 가진 여러 안드로이드 기기에 배포되는 것을 인식하여, 코드 수정 없이 기기별 UI 커스터마이징을 허용하는 유연한 구성 관리 시스템을 구현합니다.

### 실시간 데이터 파이프라인
강력한 API 통합 레이어를 활용하여 20초 갱신 간격으로 지속적인 데이터 스트림을 유지하며, 운영자가 시스템 상태를 거의 실시간으로 파악할 수 있도록 합니다.

### 모듈식 컴포넌트 설계
클린 코드 원칙에 따라 애플리케이션은 관심사를 별개의 모듈로 분리합니다:
- **네트워크 레이어**: API 통신 및 데이터 동기화 처리
- **리포지토리 패턴**: 데이터 흐름 및 캐싱 전략 관리
- **UI 컴포넌트**: 재사용 가능한 구성 가능한 인터페이스 요소
- **구성 관리**: 동적 기기 및 데이터센터 설정

## 주요 모니터링 기능

### 서버 노드 모니터링
- **Filecoin 마이닝 노드**: 마이닝 성능, 리소스 활용도, 운영 상태 추적
- **GPU 워커 노드**: 컴퓨팅 워크로드, 온도, 처리 효율성 모니터링
- **포스트 워커 시스템**: 시공간 증명 작업 및 검증 프로세스 관찰

### 스토리지 인프라
- **NAS 시스템**: 스토리지 용량, 사용 패턴, 상태 지표 표시
- **SSD/HDD 어레이**: 디스크 성능, IOPS, 마모 수준 모니터링
- **분산 스토리지**: 복제 상태 및 데이터 무결성 추적

### 네트워크 및 전력 시스템
- **100G 네트워크 스위치**: 네트워크 처리량 및 연결성 모니터링
- **UPS 컨트롤러**: 전력 이중화 및 배터리 상태 추적
- **환경 센서**: 온도, 습도, 냉각 효율성 관찰

### 특수 워크로드
- **Aethir 네트워크 노드**: 수익, 스테이킹 상태, 네트워크 참여 표시
- **AI 컴퓨팅 리소스**: 딥러닝 워크로드 및 모델 훈련 모니터링
- **블록체인 운영**: 다양한 블록체인 네트워크 참여 추적

## 기술 구현

### 데이터 시각화
시스템은 복잡한 데이터를 직관적인 형식으로 제시하기 위해 정교한 시각화 기술을 사용합니다:
- **육각형 성능 차트**: 다차원 성능 점수 제공
- **리소스 활용도 그래프**: CPU, 메모리, 스토리지 사용 추세 표시
- **섹터 기반 레이아웃**: 논리적 구성을 위한 관련 노드 그룹화
- **대화형 확장 가능 카드**: 상세 지표로 드릴다운 가능

### 적응형 UI 시스템
인터페이스는 다음을 기반으로 동적으로 조정됩니다:
- 기기 특성 및 화면 크기
- 데이터센터별 요구사항
- 노드 유형 및 고유한 모니터링 필요성
- 사용자 역할 및 액세스 수준

### 성능 최적화
24/7 연속 운영을 위해 효율성을 염두에 두고 구축:
- 메모리 효율적인 이미지 로딩 및 캐싱
- 지능형 배치를 통한 최적화된 네트워크 호출
- 코루틴 기반 비동기 작업
- 부드러운 애니메이션을 위한 하드웨어 가속

## 보안 및 액세스 제어

### 관리자 기능
시스템은 특정 상호작용 패턴을 통해 액세스 가능한 숨겨진 관리자 인터페이스를 포함하며 다음을 제공합니다:
- 데이터센터 전환 기능
- 구성 관리
- 디버그 정보 액세스
- 시스템 진단

### 데이터 보호
- 안전한 API 통신
- 암호화를 통한 구성 지속성
- 역할 기반 액세스 제어 준비

## 배포 시나리오

시스템은 다양한 운영 컨텍스트에서 배포되도록 설계되었습니다:

### 제어실 디스플레이
지속적인 모니터링을 위해 NOC(네트워크 운영 센터) 환경에 설치된 대형 화면 태블릿

### 모바일 감독
이동성이 필요한 현장 기술자 및 관리자를 위한 휴대용 기기

### 다중 화면 설정
포괄적인 커버리지를 위한 여러 기기 간 동기화된 디스플레이

## 글로벌 준비성

글로벌 프로젝트로서 시스템은 다음을 유지합니다:
- 국제 운영자를 위한 영어 기반 UI
- 다중 시간대 운영을 위한 UTC 시간 동기화
- 표준화된 메트릭 단위
- 문화적으로 중립적인 아이콘

## 미래 확장성

아키텍처는 다음을 포함한 향후 개선사항을 지원합니다:
- 추가 데이터센터 위치
- 새로운 노드 유형 및 모니터링 카테고리
- 고급 분석 및 예측 유지보수
- 외부 모니터링 시스템과의 통합
- 과거 데이터 분석 및 보고

## 시스템 요구사항

- Android 7.1.1 (API 25) 이상
- 최소 2GB RAM
- 안정적인 네트워크 연결
- 가로 방향 지원