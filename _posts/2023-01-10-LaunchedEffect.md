---
title: "Android, Compose LaunchedEffect"
classes: wide
categories:
  - Android
tags:
  - Android
  - Compose
  - LaunchedEffect
comments: true
---
<script src="https://unpkg.com/kotlin-playground@1" data-selector=".kotlin-playground"></script>

## [Jetpack Compose] Android LaunchedEffect 알아보기
최근 UI를 모두 Compose로 개발하면서 자주 사용하지만 어렴풋이 알고 사용해왔던 `LaunchedEffect`에 대해서 파헤쳐보려 한다.

### LaunchedEffect란?
Google Android Developer 페이지에서는 아래와 같이 소개하고 있다.  
`LaunchedEffect: run suspend functions in the scope of a composable`

Composable 범위에서 suspend function을 실행한다는 의미인데, ToolTip을 보면 조금 더 상세한 내용이 기술되어 있다.

![img.png](https://github.com/bcchoi0202/bblog/blob/main/assets/posts/launchedeffect.png?raw=true)

툴팁 내용을 간략히 해석해보면
>1. Composition 시 CoroutineContext 블록을 실행한다.
>2. LaunchedEffect의 키가 재구성되면 기존 Coroutine이 취소되고 다시 시작된다.
>3. Composition 종료 시 Courtine이 취소된다.
>4. 기타 등등 주의 및 안내 사항

이 정도로 요약해볼 수 있을 것 같다. 

LaunchedEffect의 실제 사용 코드는 아래와 같다.

```
LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect { page ->
        onEvent(PictureDetailContract.Event.GetPictureList(page = page))
    }
}
  
//...
    
```
 
위 코드는 HorizontalPager의 페이지가 변경되면 다음 데이터를 받아오는 코드이다.  
LaunchedEffect에 key로 pagerState를 사용해서 pagerState가 변경이 있을 때 내부의 Coroutine Block을 실행하게 된다.  

1. Horizontalpager에 대한 pagerState 생성 (rememberPagerState(initialPosition))
2. Horizontalpager Paging(1->2, 2->3, ...)
3. pagerState의 currentPage 값이 변경되어 LaunchedEffect 내부 Coroutine Block 동작

이렇게 동작하여 page가 변경되었을 경우, 다음 페이지의 데이터를 받아오는 작업을 수행하게 됩니다.

여기서는 pagerState라는 key 하나만 사용하였지만, 필요에 따라 2개, 3개...n개까지도 사용 가능하다.

여러개를 지정하면 LaunchedEffect에 지정된 key의 변경에 따라 작성한 Coroutine Block이 동작하니, 필요에 맞게 key와 block을 지정하여 사용할 수 있을 것이다.  
<br>
**마지막으로, key값을 상수로 사용하면 Composition 될 때 1회만 동작하게 된다.**


<br>
<br>
<br>


잠시 kotlin playground 추가 테스트  

<br>
<br>


<div class="kotlin-playground" theme="darcula">
  fun main() {
    val name = "stranger"        // Declare your first variable
    println("Hi, $name!")        // ...and use it!
    print("Current count:")
    for (i in 0..10) {           // Loop over a range from 0 to 10
      print(" $i")
    }
  }
</div>