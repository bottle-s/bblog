---
title: "Jekyll, Kotlin Playground 넣기"
classes: wide
categories:
  - jekyll
tags:
  - jekyll
  - Kotlin Playground
comments: true
---

블로그 포스팅을 하면서 Code 블럭을 직접 돌려볼 수 있으면 좋겠다는 생각이 들어서 찾아보았다.

## Jekyll blog에서 Kotlin Playground 사용하기
현재 blog는 .md 파일, 즉 Mark-down 언어로 포스트를 작성하기 때문에 html 태그를 어떻게 넣어야 하는가를 고민했다.  
검색을 하다보니, 어느 블로그에서는 hexo tag plugin을 넣어야 한다는 포스트도 있었고, 또 다른 블로그에서는 liquid 문법으로 써야한다는 포스트도 있었다.  

결론적으로는 kotlin playground의 script를 추가하고, kotlin-playground class로 만들어진 div 태그를 작성하면 되는 것이었다.

아래는 sample 게시물이다.

```
<script src="https://unpkg.com/kotlin-playground@1" data-selector=".kotlin-playground"></script>
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
```

이와 같이 추가하면 .. 

<script src="https://unpkg.com/kotlin-playground@1" data-selector=".kotlin-playground"></script>
<div class="kotlin-playground">
  fun main() {
    val name = "stranger"        // Declare your first variable
    println("Hi, $name!")        // ...and use it!
    print("Current count:")
    for (i in 0..10) {           // Loop over a range from 0 to 10
      print(" $i")
    }
  }
</div>

<br>

짠! 하고 실행 가능한 코드블럭이 추가된다.
여러개의 코드블럭을 추가하고 싶다면, script는 최초에 한번만 추가하면 되고, 그 이후에는 div 태그로 이루어진 코드블럭만 작성해주면 된다. 
그렇게 추가한 코드블럭 우측 상단의 플레이 버튼을 눌러주게되면... 

<div class="kotlin-playground">
  import kotlinx.coroutines.*
  fun main() = runBlocking {
    delay(1000)
    println("짜자잔")
  }
</div>

<br>

하고 결과가 나온다.

추가적으로, 기본 테마는 밝은색이다. 색을 어둡게 바꾸고 싶다면  theme="darcula" 를 div 태그 안에 넣어서 아래와 같이 사용 가능하다.
또한, import 를 통해 여러가지 기능들을 테스트 하는데에 용이할 것으로 생각된다.

<div class="kotlin-playground" theme="darcula">
  import kotlinx.coroutines.*
  import kotlinx.coroutines.flow.*
  fun main() {
      val str = "Flow emit() 테스트하면 이렇게 나옵니다!!"
      CoroutineScope(Dispatchers.Default).launch {
          emitText(str).collect {
              print("$it")
          }
      } 
  }
  
  private suspend fun emitText(str: String) = flow {
    str.forEach {
      emit("$it")
    }
  }
</div>

