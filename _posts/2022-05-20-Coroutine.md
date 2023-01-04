---
title: "Android, Coroutine"
classes: wide
categories:
- Android
  tags:
- Android
- Coroutine
---

Android에서는 꽤 오래전부터 rxJava 보다 Coroutine을 더 많이 사용하는 추세로 변해가고 있다.  
Coroutine이란 무엇인지와 종류, 특징, 그리고 어떻게 사용하는지에 대하여 간략하게 적어보려 한다.  
또한 추가적으로 rx와의 간단한 비교도 함께 해볼 예정이다.  

1. Coroutine 이란?
  1) Coroutine의 역사
    - 1958년부터 만들어진 용어로, 협력을 뜻하는 co와 routine의 합성어
    - Assembly 프로그램부터 시작하여 C++, C#, Unity 등 여러곳에서 이미 사용되고 있던 개념을 Android에 적합하게 변경하여 사용

  2) Coroutine의 종류
     (1) Stackful Coroutine
     - 자신만의 Stack을 보유하고 있으며 호출한 코드로부터 독립적인 활동
     - 할당된 Stack에 인수와 변수를 저장하여 Coroutine 내에서 호출되는 모든 함수의 실행 중단이 가능

     (2) Stackless Coroutine
     - 자신만의 Stack을 보유하고 있지 않아 Context의 Stack을 사용하므로 호출한 Context(Caller)와 강하게 연결
     - Stack이 없기 때문에 훨씬 적은 Memory를 사용
     - Context의 수명만큼 유지되며 Context destroy 시 cancel
     -

2. Coroutine의 특징
  1) 가독성
    - Coroutine의 경우 scope 내에서 순차적으로 작성하며 suspend 함수롤 호출해나가기 때문에 Rx에 비하여 기존 코드 모양이 유지되며 가독성이 향상된다.

  2) 효율성
    - 모바일 사용자의 환경에서 사용 리소스가 줄고 속도가 향상된다 함은 배터리 및 퍼포먼스와 직결되며, 결국 사용자의 사용성에 지대한 영향을 미칠 수 있다.

  3) Stackless
    - Coroutine은 Stackless로 자체적인 Stack을 갖고 있지 않아 메모리의 사용량이 적음
    - 자체 Stack을 갖고있지 않은 대신 Coroutine을 호출한 Caller의 Stack을 사용

  4) Context Switching
    - 특정 Thread에 종속되지 않기 때문에 Context Switching이 필요 없다.

3. Coroutine 기본
  1) Coroutine Builder
     (1) launch
     - 결과값 반환 없음

     (2) async
     - 결과값 반환
     - 병렬처리에도 사용

  2) Scope
     (1) GlobalScope
     - Application의 생명주기에 따라 동작
     - App이 실행되고 종료될때까지 실행되어야 하는 Coroutine에 사용

     (2) LifecycleScope
     - LifecycleOwner의 생명주기와 함께 동작

     (3) CoroutineScope
     - Custom Scope로 원하는 위치에서 Coroutine을 시작

     (4) ViewModelScope
     - ViewModel에서 instance의 생명주기에 따라 동작

     (5) SupervisorScope
     - Custom Scope로 원하는 위치에서 Coroutine을 실행할 수 있지만 Error에 대한 전파가 부모에게는 이루어지지 않음
     - 여러개의 subCoroutine 중 한개가 실패하여도 나머지는 계속 실행되어야 할 때(?) 사용

  3) Dispatcher
     (1) Dispatcher란?
        - 각각의 작업을 Processor(Thread)에 할당

     (2) Dispatcher의 종류
        - Default
          : CPU를 많이 쓰는 작업에 최적화 되어있는 Dispatcher
        -  IO
          : Network, Database, File I/O 등 입출력 작업에 최적화 되어있는 Dispatcher
        - Main
          : UI 작업 관련 Dispatcher
        - Unconfined
          : 호출한 Context를 기본으로 사용하는 Dispatcher.
            작업이 중단된 후 다시 실행될 때 Context에 변경이 있다면, 변경된 Context로 기존 Context가 변경



Rx와 비교?
- Rx 대비 적은 리소스를 사용하며 수행 속도가 더욱 빠르다.
- 가독성이 좋음

Result
: Rx로 작업되어있던 코드보다 가독성이 좋아짐
속도면에서는 큰 차이를 느끼기 힘듦
전체적으로 코드의 라인수가 줄어들고 유지보수가 쉬움