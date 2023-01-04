---
title: "Android, Architecture Pattern"
classes: wide
categories:
- Android
  tags:
- Android
- Design Pattern
- Architecture Pattern
---
기본적으로, 프로젝트 구조 설계 시에는 어떠한 Architecture Pattern을 적용할 것인지 검토하게 된다.  
먼저, **Architecture Pattern이란 무엇인지**, 그리고 우리가 사용하는 **Android의 Architecture Pattern에는 어떠한 것이 있는지**, **현재는 무엇을 가장 많이 사용하는지**, 그리고 **왜 그런지**에 대해 간략히 알아보자.  

1. Architecture Pattern이란?
   - Software의 구조를 구성하기 위한 기본적인 윤곽을 일컫는다. 
   - 각각의 Sub-System에 역할이 정의되어 있고, 관계 및 규칙 등이 포함된다. 
2. Architecture Pattern을 왜 사용해야 하는가
   - 개발시에 발생하는 다양한 에러의 원인과 내용들을 쉽게 파악
   - 시행착오가 줄어들어 개발시간이 단축
   - 공통 Architecture를 사용하여 협업 시 서로의 코드 분석 용이
   - Coupling 감소로 유지보수 및 확장성 증대 
3. Android Architecture Pattern 흐름
굉장히 많은 Architecture Pattern이 존재하지만, Android 개발에 사용되어온 패턴의 흐름만 간략하게 소개하도록 한다.
아래 각 패턴에 대하여 설명을 하겠지만, 필자의 주니어 시절부터 지금까지 사용하는 패턴은 다음과 같다
> MVC -> MVP -> MVVM

각 패턴은 각 모듈간의 의존성을 감소시키기 위하여(커플링을 약화시키기 위하여) 점차 개선되어 왔다.  
이제 위에서 언급된 패턴의 순서를 기억해두고, 각 패턴의 흐름, 장/단점에 대하여 알아보자.  

4. Architecture Pattern 종류
  1) MVC
     1.1) 정의
     - Model - View - Controller 구조로 이루어진 Design Pattern
     - Model class는 별도로 작성되어지지만, View와 Controller는 Activity 또는 Fragment 등 한개의 class에 모두 작성

     1.2) 흐름
     - UI코드와 Model 코드를 분리하고 이 둘을 처리하는 Controller 코드를 작성
     - Controller(사용자 이벤트 입력) -> Model(데이터 갱신) -> View(UI 업데이트)
       ![MVC](/assets/posts/mvc.png)

     1.3) 장점
     - 하나의 Activity / Fragment 안에서 모두 작성하여 사용하니 개발이 빠르고, 직관적
     - 개발을 직접 하지 않은 사람도 코드만 보면 바로 파악이 가능

     1.4) 단점
     - 하나의 class에 모든 코드가 들어있다 보니, 코드 라인수가 길어짐
     - 개발 시 관심사 분리를 적절히 하지 않으면 유지보수가 어려워짐
     - View와 Model의 결합도가 높기 때문에 테스트 코드의 작성이 어려움

  2) MVP
     2.1) 정의
     - MVC에서 발전되어 Model - View - Presenter 구조로 이루어진 Design Pattern
     - MVC와 거의 동일하지만 View와 Model의 의존성을 제거하여 단위테스트 작성의 어려움을 해소

     2.2) 흐름
     - View는 Model을, Model은 View를 서로 참조할 수 없도록 개발하며, 모든 것은 Presenter를 통해서만 주고받을 수 있도록 작성되어 있다.
     - View(사용자 이벤트 입력) -> Presenter(Model로 데이터 전달) -> Model(데이터 호출) -> Presenter(Model에서 데이터 수신) -> View(UI갱신)
       ![MVP](/assets/posts/mvp.png)

     2.3) 장점
     - Model과 View를 분리하여 MVC 대비 코드가 깔끔하며 확장이 용이
     - 관심사가 분리되어 유지보수가 용이

     2.4) 단점
     - 설계에 따라 View와 Presenter의 의존성이 강해지기 때문에 코드 양이 증가함에 따라 분리하기 어려워질 수 있음

  3) MVVM
     3.1) 정의
     - Model - View - ViewModel 구조로 이루어진 Design Pattern

     3.2) 흐름
     - Model은 아무것도 참조하지 않고 ViewModel은 Model을, View와 ViewModel은 의존관계가 없음
     - View(사용자 이벤트 입력) -> ViewModel -> Model -> ViewModel -> (Binding)xml
     ![MVVM](/assets/posts/mvvm.png)

     3.3) 장점
     - 기존 MVP에서 View와 Presentor의 의존성이 높아지는 부분을 해소
     - View와 Model이 서로 참조하지 않아 독립성을 유지할 수 있으며 테스트에 용이
     - Databinding을 이용하여 전체적인 코드의 양 감소 
     - View를 참조하지 않기 때문에 동일한 ViewModel을 다른 View에서도 사용 가능(View:ViewModel = N:1)
  
  > MVI 란?
  > 요즘은 MVI라는 Model - View - Intent 개념이 자주 나오고 있다.
  > 결론적으로, MVI는 Architecture Pattern이라기 보다는 MVVM의 상태 문제와 부수효과를 어떻게 처리하느냐에 대해 다루는 패러다임이라고 볼 수 있을 것 같다.
  > ![MVI](/assets/posts/mvi.png)
  > **정의**
    - 행위가 단방향으로 이루어지며, 그 방향이 Cycle을 이루고 있는 Cycle.js 프레임워크에서 따왔으며, 기존 Architecture Pattern과는 매우 다르게 작동
    - MVVM의 문제라고 일컬어는 상태문제 및 부수효과 처리에 대한 해결책으로 제시 
    - Model - View - Intent 구조로 이루어진 Design Pattern
  > **흐름**
    - View에서 Event가 입력되면 Intent로 전달하고 Intent에서는 입력된 Event를 기준으로 Model을 변경하며, Model에서는 데이터 및 View의 상태를 변경한다.
  > <br>
  > 간략히 알아보았지만, 결국 MVI란 기존의 Architecture Pattern에 View의 상태처리와 Side Effect(Toast, Snackbar 등)를 함께 제어하여 문제가 발생하지 않도록 처리하는 패러다임인 것으로 볼 수 있겠다.

5. Finally
Architecture Pattern은 쉽지 않다. 사용하고자 하는 패턴의 구조와 원리 등 여러가지를 확실히 파악 하고 프로젝터 설계 시 어떻게 적용시킬 것인가를 여러모로 고민하여 개발에 임해야 할 것이다.