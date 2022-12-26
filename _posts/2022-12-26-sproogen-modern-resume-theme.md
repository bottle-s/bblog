---
title: "modern-resume-theme: github page 이력서 만들기 (feat. jekyll, modern-resume-theme)"
categories:
  - jekyll-theme
tags:
  - jekyll
  - theme
  - resume
  - github 이력서
---

Android 개발자 이력서 작성을 위하여 github page를 만들기로 결심하고, 개발자 이력서를 여기저기 찾아 헤메였다.  
공개 이력서를 이곳 저곳 기웃기웃 하던 중에 [이동욱 개발자님 이력서](https://jojoldu.github.io/)를 보고 첫눈에 반해 관련 테마를 검색하게 되었고, jekyll theme 중 [modern-resume-theme](https://github.com/sproogen/modern-resume-theme) 를 알게 되었다.  
<br>
여러가지 테마로 github page, blog를 만들기 위해서 작업 해봤지만, 단순 이력서만 작성하기에는 간편하고 깔끔하다고 생각해서 사용하게 되었다.  
[필자 이력서](https://bcchoi0202.github.io/r/)  

작업하겠다고 마음을 먹었으니 뚱땅뚱땅 작업을 시작해본다.  

본 게시물에서 사용한 테마를 사용하실 분들은 아래의 순서대로 따라하면 간단하게 적용할 수 있으니, 필요하다면 따라해보시길 권한다.  

늘 그렇듯, [modern-resume-theme](https://github.com/sproogen/modern-resume-theme) github repository에 방문하여 README.md 페이지를 읽어준다.  
간략히 순서가 나와있는데, 간단히 얘기하면 theme 소스를 다운받아서 내 github page repository에 push 하는 것이다.  

처음에는 모든 폴더를 받아서 commit & push 했지만, 모두 다 할필요는 없고 아래 스크린샷에 포함된 폴더 및 파일만 업로드 해도 된다.  
![img_1.png](assets/image/tree.png)  
<br>
위의 tree 대로 푸시를 하고 일정시간 기다린 뒤 github page를 새로고침 해보면 나면 테마가 적용된 페이지가 보인다.  

필자는 환경 설정을 할 수 없는 상황이라 push 후 업데이트 확인을 반복하였는데, 로컬에서 테스트를 할 수 있으니 [jekyll local test](https://docs.github.com/ko/enterprise-server@3.6/pages/setting-up-a-github-pages-site-with-jekyll/testing-your-github-pages-site-locally-with-jekyll) 페이지를 참조하여 해보시기 바란다.  


무튼, 기본적인 셋업은 끝났고.. 본격적인 페이지 작성을 시작한다.  
기본 설정은 우리가 github에 push 한 코드 중 _config.yml 파일을 열어보면 된다.  
<br>
기본적으로 주석이 꼼꼼하게 달려있어서 작성하기는 정말 쉬운편이다.
- Social links
  - 각 소셜미디어의 id를 적어주면 된다. 적지 않으면 해당 소셜미디어는 비활성화 된다. 
- Additional icon links
  - 사용자가 추가할 임의의 링크 및 아이콘을 추가한다. 필자는 별도의 사이트를 운영하지 않았기 때문에(보잘것 없기 때문에) 추가하지 않았다.
- About Section
  - '나'에 대한 정보를 표시하는 섹션으로, 타이틀과 프로필 이미지, 그리고 나를 소개하는 컨텐츠 등을 업데이트 할 수 있다.
- content
  - 이후 섹션은 표기하고 싶은 항목에 따라 구분지어서 사용할 수 있다.
  - 다들 그렇듯, 필자는 Skill-stack, Experience, Education, In ordinary days 등으로 구분지어 작성하였다.

편하게 사용하고자 한다면, 필자의 [github](https://github.com/bcchoi0202/r)에 업로드 되어있는 _config.yml 파일을 참고하여 수정한다면 더욱 간단하게 사용할 수 있을 것이다.
    

