---
title: "Android, DatePicker(아이템 3개 이상 보이게 설정하기)"
classes: wide
categories:
  - Android
tags:
  - Android
  - DatePicker
  - Spinner
  - NumberPicker
comments: true
---


## [DatePicker] Android Multiple Item Visible DatePicker 
최근 UI를 개발하며 난관을 겪었던 Datepicker에 대해 어떤 이슈가 있었는지와 어떤 식으로 구현하였는지에 대하여 간략히 소개하려 한다.

### DatePicker란?
Android 에서 제공하는 DatePicker는 문자 그대로 Date를 Pick한다, 즉 **날짜 선택기** 정도로 풀이할 수 있다. 
그렇다면, 날짜를 선택하는 방법에는 어떤 것들이 있는지 간략히 알아보자.

#### 1.Calendar
Calendar 형태는 달력이 표시되어 년/월/일을 선택하는 개념이다. 
DatePicker 실행 시 Default로 설정한 날짜의 달력이 표시되며, 표시된 달력에서 년/월/일을 선택하게 된다.(선택 가능 필드는 필요에 따라 Custom 가능하다.)

커스텀하지 않은 기본 DatePicker는 아래와 같다.
![img.png](https://github.com/bcchoi0202/bblog/blob/main/assets/posts/datepicker/datepicker.png?raw=true)


위와 같이 나온 달력에서 필요한 날짜를 선택하여 사용할 수 있다.

#### 2. Spinner
Spinner 형태는 NumberPicker가 여러개 출력되어 각 Picker를 드래그하여 년/월/일을 선택할 수 있도록 구성되어 있다.
![img.png](https://github.com/bcchoi0202/bblog/blob/main/assets/posts/datepicker/datepicker_spinner.png?raw=true)

각 Spinner의 가운데에 원하는 날짜를 맞춰 필요한 날짜를 사용할 수 있다.

### 이슈가 무엇인가?
먼저 디자인팀에서 보내준 시안을 보자.
![img.png](https://github.com/bcchoi0202/bblog/blob/main/assets/posts/datepicker/damoum_datepicker.png?raw=true)

시안에 따르면 요구사항은 아래와 같이 정리해볼 수 있다. 
1. DatePicker의 Spinner 형태로 구현된 **5개 까지 보이는** 날짜 선택 기능 필요
2. 각 Spinner를 통해 Dragging을 통하여 날짜를 선택
3. 선택된 날짜(년/월/일) 에는 특정 Color로 Highlight
4. 상단/하단에는 Fadding Edge에 대한 처리도 진행해야 했다.

요구사항도 명확하며(이 외에도 더 있긴 하지만), 사실 시안만 보고, '금방 할 수 있겠네' 라는 생각이 들었다.
실제로 개발을 시작하면서 DatePicker를 Custom 하기 시작했는데, 시작하면서 부터 장벽에 부딪히게 되었다.

***DatePicker의 spinnerMode에 visibleItemCount를 설정할 수가 없다는 것이다.***

믿을수가 없어서 DatePicker를 까서 보기 시작했다.
![img.png](https://github.com/bcchoi0202/bblog/blob/main/assets/posts/datepicker/inner_datepicker.png?raw=true)

DatePicker 생성자에 Mode를 설정하도록 되어있고, 따라가보면 DatePickerSpinnerDelegate를 통해 UI를 설정하도록 되어있다.

![img.png](https://github.com/bcchoi0202/bblog/blob/main/assets/posts/datepicker/inner_datepicker_spinner.png?raw=true)

그리고는 NumberPicker로 캐스팅 되어있는 yearSpinner, monthSpinner, daySpinner를 볼 수가 있었다.

'아, NumberPicker에서 보이는 개수를 설정하면 되겠구나' 라고 생각하였지만, 그 또한 불가능했다.
먼저 NumberPicker에 들어가서 보면 SELECTOR_WHEEL_ITEM_COUNT라는 변수에 3으로 보이는 아이템 수를 설정하고 있다.
하지만, setter도 없는 private으로 되어있기 때문에 해당 값을 바꾸기가 불가능했다.

그렇게 간단할 것 같던 5개짜리 DatePicker는 구렁텅이로 빠지기 시작했다. 

상용 라이브러리도 찾아보고 참고할만한 여러곳을 찾아봤지만, 결국 건진것은 없었기에 어떻게 구현할까 하다가 android.widget에 있는 NumberPicker를 Custom하기로 했다.
상속받아 구현해도 해당 상수는 변경할 수가 없기 때문에, android.widget.NumberPicker에서 제공하는 NumberPicker를 그대로 구현하기로 했다.

해당 코드는 java로 되어있어서 kotlin으로 변환해야하는데 여기서도 시간이 꽤 들어갔다. 

그렇게 해서 만들어진 결과물은 아래와 같다.
![img.png](https://github.com/bcchoi0202/bblog/blob/main/assets/posts/datepicker/real_datepicker.png?raw=true)

간단하게 생각했는데 생각보다 많이 힘들게 만들어서 이렇게 포스팅 해본다.

또한, 나처럼 필요한 사람들이 있을 것이기 때문에, 필요하다면 소스를 커스텀해서 사용하면 된다.

단, 사용하게 되면 흔적은 조금 남겨주시길 바란다.

> [소스코드 확인하기](https://github.com/bcchoi0202/bblog/blob/main/assets/posts/datepicker/WheelSpinner.kt)
