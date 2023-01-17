---
layout: archive
permalink: /categories/jekyll
title: "Jekyll"
author_profile: true
sidebar:
  nav: "docs"
---
{% assign posts = site.categories.Jekyll %}
{% for post in posts %} {% include archive-single.html type=page.entries_layout %} {% endfor %}