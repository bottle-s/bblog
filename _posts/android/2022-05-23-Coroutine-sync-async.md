---
title: "Android, Coroutine 동기 / 비동기 처리"
classes: wide
categories:
- Android
  tags:
- Android
- Coroutine
  comments: true
---

Coroutine을 본격적으로 사용하기 전에 코루틴이 어떻게 해야 동기처리 되는지, 그리고 어떻게 해야 비동기 처리 되는지 알아보고자 한다.

먼저, 간단한 코드를 보고 설명하겠다.

```
fun main() {
    CoroutineScope(Dispatchers.Default).launch {
        drawHead()
        drawBody()
        drawLegs()
    }.join()
}

suspend fun drawHead() {
    delay(1000)
}

suspend fun drawBody() {
    delay(2000)
}

suspend fun drawLegs() {
    delay(3000)
}
```

위 코드는 순차적으로 동작했을 경우 6초가, 비동기로 동작했을 경우에는 3초가 걸리는 코드이다. 

어떻게 동작할까 ?

Coroutine으로 만들었으니 비동기로 동작해서 3초걸리겠지 ! 할 수도 있겠으나, 위 코드는 하나의 Coroutine Builder에서 모든 서브루틴을 호출했기 때문에 순차적으로 동작한다.

<script src="https://unpkg.com/kotlin-playground@1" data-selector=".kotlin-playground"></script>
<div class="kotlin-playground" theme="darcula">
import kotlinx.coroutines.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun main() = runBlocking {
    println("make person start")
    measureTime {
        CoroutineScope(Dispatchers.IO).launch {
            drawHead()
            drawBody()
            drawLegs()
        }.join()
   }.also {
    println(it)
  }
  println("make person end")
}

suspend fun drawHead() {
    println("drawHead start")
    delay(1000)
    println("drawHead end")
}

suspend fun drawBody() {
    println("drawBody start")
    delay(3000)
    println("drawBody end")
}

suspend fun drawLegs() {
    println("drawLegs start")
    delay(2000)
    println("drawLegs end")
}
</div>

출력된 로그와 같이 머리를 그리고, 끝나면 몸을 그리고, 몸을 그리는게 끝나면 다리를 그린다.   
이렇게 순차적으로 진행되어 6초가량 걸리는 것을 확인할 수 있다. 


그렇다면, 이 코드를 비동기로 작동시키고 싶다면 어떻게 해야할까?

각각의 서브루틴을 코루틴빌더로 빌드하면 된다.

아래의 코드는 하나의 코루틴 빌더 내에서 각각을 한번 더 코루틴 빌더로 감싸주었다.  
이 코드는 얼마나 걸리는지 실행해보자.

<div class="kotlin-playground" theme="darcula">
import kotlinx.coroutines.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun main() = runBlocking {
    println("make person start")
    measureTime {
        CoroutineScope(Dispatchers.IO).launch {
            launch { drawHead() }
            launch { drawBody() }
            launch { drawLegs() }
        }.join()
    }.also {
        println(it)
    }
    println("make person end")
}

suspend fun drawHead() {
    println("drawHead start")
    delay(1000)
    println("drawHead end")
}

suspend fun drawBody() {
    println("drawBody start")
    delay(3000)
    println("drawBody end")
}

suspend fun drawLegs() {
    println("drawLegs start")
    delay(2000)
    println("drawLegs end")
}
</div>

각각의 서브루틴을 코루틴 빌더로 감쌌더니 비동기로 처리가 되어 3초가량 소요되었다.  

이렇듯, 비동기코드를 작성할 때와 동기코드를 작성할 때를 구분해서 필요에 따라 동기/비동기 코드를 작성할 수 있을 것이다.  