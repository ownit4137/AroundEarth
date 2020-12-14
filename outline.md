# AroundEarth

## 서버 측 코드

### 게임 시작 전

- 참여 인원 설정	[**입력**]
- 지구인(Terran), 스크럴(Skrull) 인원 설정 (자동 or  수동)
- 인원 참여까지 대기	[**network**]
- 모두 참여 시 5초 후 시작	[**cyclicbarrier**] [**client에 메세지 출력**]

### 게임 진행

- <span style="color:red">Skrull</span>, Terran
- 밤 상태로 시작

### 진행 - 밤

- Skrull은 

- 밤 상태로 시작
- 마피아 탐색

### 결과

- 결과

## 클라이언트 측 코드

### 게임 시작 전

- 접속 후 대기	[**network**]
- 5초 출력	[**server에서 메세지를 받음**]
