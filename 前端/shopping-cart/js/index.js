/**
 * 单件商品
 * ES6语法，等效于ES5的原型链
 */
class UIGoods {
  constructor(goods) {
    this.data = goods;
    this.choose = 0; // 已选的数量
  }

  /**
   * 总价
   */
  getTotalPrice() {
    return this.data.price * this.choose;
  }

  /**
   * 是否选中
   */
  isChoose() {
    return this.choose > 0;
  }

  /**
   * 选择数量+1
   */
  increase() {
    this.choose++;
  }

  /**
   * 选择数量-1
   */
  decrease() {
    if (this.choose <= 0) {
      return;
    }
    this.choose--;
  }
}

class UIData {
  constructor() {
    this.uiGoods = goods.map((ug) => {
      return new UIGoods(ug);
    });
    this.deliveryThreshold = 30; // 起送费
    this.deliveryPrice = 5; // 配送费
  }

  getTotalPrice() {
    return this.uiGoods.reduce((sum, ug) => {
      return sum + ug.getTotalPrice();
    }, 0);
  }

  /**
   * 增加某件商品的选中数量
   */
  increase(index) {
    this.uiGoods[index].increase();
  }

  decrease(index) {
    this.uiGoods[index].decrease();
  }

  isChoose(index) {
    return this.uiGoods[index].isChoose();
  }

  /**
   * 总共选中多少件商品
   */
  getTotalChooseCount() {
    return this.uiGoods.reduce((sum, ug) => {
      return sum + ug.choose;
    }, 0);
  }

  /**
   * 购物车中是否有商品
   */
  hasGoodsInCart() {
    return this.getTotalChooseCount() > 0;
  }

  /**
   * 是否达到起送门槛
   */
  isReachDeliveryThreshold() {
    return this.getTotalPrice() >= this.deliveryThreshold;
  }
}

class UI {
  constructor() {
    this.uiData = new UIData();
    this.doms = {
      goodsContainer: document.querySelector(".goods-list"),
      deliveryPrice: document.querySelector(".footer-car-tip"), // 配送费
      footerPay: document.querySelector(".footer-pay"),
      footerPayInnerSpan: document.querySelector(".footer-pay span"),
      totalPrice: document.querySelector(".footer-car-total"),
      cart: document.querySelector(".footer-car"),
      badge: document.querySelector(".footer-car-badge"),
    };
    this.createHTML();
    this.updateFooter();
    this.listenEvent(); // 事件只注册一次
  }

  listenEvent() {
    // 动画完成之后去除样式，让下次再加样式时也有动画
    this.doms.cart.addEventListener("animationend", function () {
      this.classList.remove("animate");
    });
    var that = this;
    this.doms.goodsContainer.addEventListener("click", function (e) {
      if (e.target.classList.contains("i-jiajianzujianjiahao")) {
        that.increase(+e.target.dataset.index);
      } else if (e.target.classList.contains("i-jianhao")) {
        that.decrease(+e.target.dataset.index);
      }
    });
  }

  /**
   * 根据商品数据创建列表元素
   * 1. 生成HTML字符串
   * 2. 一个一个创建元素
   */
  createHTML() {
    var html = '';
    for (var i = 0; i < this.uiData.uiGoods.length; i++) {
      var ug = this.uiData.uiGoods[i];
      html += `<div class="goods-item">
        <img src="${ug.data.pic}" alt="" class="goods-pic" />
        <div class="goods-info">
          <h2 class="goods-title">${ug.data.title}</h2>
          <p class="goods-desc">${ug.data.desc}</p>
          <p class="goods-sell">
            <span>月售 ${ug.data.sellNumber}</span>
            <span>好评率${ug.data.favorRate}%</span>
          </p>
          <div class="goods-confirm">
            <p class="goods-price">
              <span class="goods-price-unit">￥</span>
              <span>${ug.data.price}</span>
            </p>
            <div class="goods-btns">
              <i data-index="${i}" class="iconfont i-jianhao"></i>
              <span>${ug.choose}</span>
              <i data-index="${i}" class="iconfont i-jiajianzujianjiahao"></i>
            </div>
          </div>
        </div>
      </div>`;
    }
    this.doms.goodsContainer.innerHTML = html;
  }

  increase(index) {
    this.uiData.increase(index);
    this.jump(index);
  }

  decrease(index) {
    this.uiData.decrease(index);
    this.cartAnimate(index);
  }

  updateGoodsItem(index) {
    var goodsDOM = this.doms.goodsContainer.children[index];
    if (this.uiData.isChoose(index)) {
      goodsDOM.classList.add("active");
    } else {
      goodsDOM.classList.remove("active");
    }
    var span = goodsDOM.querySelector(".goods-btns span");
    span.textContent = this.uiData.uiGoods[index].choose;
  }

  updateFooter() {
    var total = this.uiData.getTotalPrice();
    this.doms.deliveryPrice.textContent = `配送费￥${this.uiData.deliveryPrice}`;
    // 到达起送
    if (this.uiData.isReachDeliveryThreshold()) {
      this.doms.footerPay.classList.add("active");
    } else {
      this.doms.footerPay.classList.remove("active");
      // 还差多少钱
      var dif = Math.round(this.uiData.deliveryThreshold - total);
      this.doms.footerPayInnerSpan.textContent = `还差￥${dif}元起送`;
    }
    // 总价
    this.doms.totalPrice.textContent = total.toFixed(2);
    // 购物车状态
    if (this.uiData.hasGoodsInCart()) {
      this.doms.cart.classList.add("active");
    } else {
      this.doms.cart.classList.remove("active");
    }
    // 购物车中商品数量
    this.doms.badge.textContent = this.uiData.getTotalChooseCount();
  }

  /**
   * 购物车抖动动画
   */
  cartAnimate(index) {
    this.doms.cart.classList.add("animate");
    this.updateGoodsItem(index);
    this.updateFooter();
  }

  /**
   * 抛物线跳跃
   */
  jump(index) {
    var cartRect = this.doms.cart.getBoundingClientRect();
    var jumpTartget = {
      // 视口可能会变化
      x: cartRect.left + cartRect.width / 2,
      y: cartRect.top + cartRect.height / 5,
    };
    var btnAdd = this.doms.goodsContainer.children[index].querySelector(
      ".i-jiajianzujianjiahao"
    );
    var rect = btnAdd.getBoundingClientRect();
    var jumpStart = {
      x: rect.left,
      y: rect.top,
    };
    // console.log(jumpStart, jumpTartget);
    var div = document.createElement("div");
    div.className = "add-to-car";
    div.style.transform = `translateX(${jumpStart.x}px)`;
    var i = document.createElement("i");
    i.className = "iconfont i-jiajianzujianjiahao";
    i.style.transform = `translateY(${jumpStart.y}px)`;
    div.appendChild(i);
    var that = this;
    div.addEventListener(
      "transitionend",
      function () {
        div.remove();
        that.cartAnimate(index);
      },
      {
        once: true, // 事件仅仅触发一次，否则i会事件冒泡
      }
    );
    // 强行渲染
    // div.clientWidth;
    requestAnimationFrame(function () {
      document.body.appendChild(div);
      requestAnimationFrame(function () {
        div.style.transform = `translateX(${jumpTartget.x}px)`;
        i.style.transform = `translateY(${jumpTartget.y}px)`;
      });
    });
  }
}

var ui = new UI();
console.log(ui);
