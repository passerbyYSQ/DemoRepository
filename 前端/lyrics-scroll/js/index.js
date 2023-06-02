/**
 * 转换数据结构：将歌词字符串转换为对象
 * [{ time: 1.06, words: 'xxxx' }]
 */
function parseLrc() {
  var lines = lrc.split("\n");
  var res = [];
  for (var i = 0; i < lines.length; i++) {
    var str = lines[i];
    var parts = str.split("]");
    res.push({
      time: parseTime(parts[0].substring(1)),
      words: parts[1].trim(),
    });
  }
  return res;
}

/**
 * 将时间字符串解析为数字
 * @param {*} timeStr 00:34.15
 * @returns 单位为秒的整数
 */
function parseTime(timeStr) {
  var parts = timeStr.trim().split(":");
  return +parts[0] * 60 + +parts[1];
}

var lrcData = parseLrc();
console.log(lrcData);
var doms = {
  audio: document.querySelector("audio"),
  ul: document.querySelector(".container ul"),
  container: document.querySelector(".container"),
};
doms.audio.volume = 0.1;

/**
 * 计算出当前情况下（播放到第几秒），lrcData数组中应该高亮显示的歌词下标
 */
function findIndex() {
  var currSec = doms.audio.currentTime;
  // 二分查找第一个小于等于currSec的元素下标
  var l = 0,
    r = lrcData.length - 1; // r = lrcData.length 时如果找不到就会越界
  while (l < r) {
    // https://leetcode.cn/problems/search-insert-position/solution/te-bie-hao-yong-de-er-fen-cha-fa-fa-mo-ban-python-/
    // floor：l = mid + 1； ceil：r = mid - 1
    var mid = Math.ceil((l + r) / 2);
    if (lrcData[mid].time <= currSec) {
      l = mid;
    } else {
      r = mid - 1;
    }
  }
  return l;
}

/**
 * 创建歌词元素li
 */
function createLrcElements() {
  var frag = document.createDocumentFragment();
  for (var i = 0; i < lrcData.length; i++) {
    var li = document.createElement("li");
    li.textContent = lrcData[i].words;
    // 每次append都改动了dom树
    //doms.ul.appendChild(li);
    frag.appendChild(li);
  }
  doms.ul.appendChild(frag);
}
createLrcElements();

// 不要写在setLrcOffset()中，因为获取clientHeight会导致reflow，而setLrcOffset()是频繁调用的
var containerHeight = doms.container.clientHeight;
var liHeight = doms.ul.children[0].clientHeight;
var maxOffset = doms.ul.clientHeight - containerHeight;

/**
 * 设置歌词偏移量
 */
function setLrcOffset() {
  var index = findIndex();
  var offset = liHeight * index + liHeight / 2 - containerHeight / 2;
  if (offset < 0) {
    offset = 0;
  }
  if (offset > maxOffset) {
    offset = maxOffset;
  }
  var activeLi = doms.ul.querySelector(".active");
  if (activeLi) {
    activeLi.classList.remove("active");
  }
  doms.ul.style.transform = `translateY(-${offset}px)`;
  doms.ul.children[index].classList.add("active");
  return offset;
}

doms.audio.addEventListener("timeupdate", setLrcOffset);
