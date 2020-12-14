# AroundEarth

## 서버 측 코드

### 게임 시작 전

- 참여 인원 5
- 지구인(Terran), 스크럴(Skrull) 인원 설정 (Skrull 1, Terran 4)
- 인원 참여까지 대기	[**network**]
- 모두 참여 시 5초 후 시작	[**cyclicbarrier**] [**client에 메세지 출력**]

### 게임 시작

- <span style="color:red">Skrull</span>, Terran
- 밤 상태로 시작
- 각각의 플레이어에게 1~5의 숫자가 하나씩 부여됨
- <span style="color:red">Skrull은 4나 5가 부여됨</span>

### 진행 - 밤

- 밤의 지속 시간은 15초
- <span style="color:red">Skrull은 한 명을 죽일 수 있음</span>

### 진행 - 낮 : 대화

- 자신의 번호 출력
- 대화의 지속 시간은 30초
- 자유로운 대화	[**플레이어 이름, 대화 내용**]

### 진행 - 낮 : 투표

- 각각의 플레이어가 의심되는 사람을 투표
- 투표 시간은 10초
- _투표 현황 출력?_
- 투표로 한 사람이 처형됨 	[**thread stop**]
- 다득표가 동률이면 넘어감
- _무투표 표시?_

### 종료 조건 

- 밤 -> 낮 전환 시 Terran 0 일때 <span style="color:red">Skrull 승리</span>
- 낮 -> 밤 전환 시 Skull 0일때 Terran 승리


## 클라이언트 측 코드

### 게임 시작 전

- 닉네임 입력 _글자수 제한?_
- 게임 룰 출력
- 접속 후 인원 대기	[**network**]
- 5초 출력 후 시작	[**server에서 메세지를 받음**]

### 게임 시작

- 자신의 숫자 표시


### 진행 - 밤

- <span style="color:red">Skrull은 죽일 사람을 선택, 닉네임 입력</span>
