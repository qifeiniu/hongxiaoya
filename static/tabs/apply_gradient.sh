#!/bin/bash
cd /Users/niuqifei/Desktop/6.2红小鸭相亲交友APP/frontend/src/static/tabs

for file in home discover activity message profile; do
  # 提取路径内容
  content=$(cat "$file"-active.svg | sed 's/.*<svg[^>]*>//' | sed 's/<\/svg>//')
  
  cat << SVGEOF > "$file"-active.svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
  <defs>
    <linearGradient id="grad-$file" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#A08FFF" />
      <stop offset="100%" stop-color="#6B5EF7" />
    </linearGradient>
  </defs>
  <g fill="url(#grad-$file)">
    $content
  </g>
</svg>
SVGEOF
done
