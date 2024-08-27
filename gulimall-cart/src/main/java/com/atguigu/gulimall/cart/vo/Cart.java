package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class Cart {
   private List<CartItem> items;
   private Integer countNum;//商品数量
    private Integer countType;//商品类型数量
    private BigDecimal totalAmount;//商品总价
    private BigDecimal reduce=new BigDecimal("0 ");//减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count=0;
        if(items!=null && items.size()>0){
            for (CartItem item : items) {
                count+=item.getCount();
            }
        }
        return count;
    }


    public Integer getCountType() {
        return items==null?0:items.size();
    }



    public BigDecimal getTotalAmount() {
        BigDecimal amount=new BigDecimal("0");
        //计算购物项总价
        for (CartItem item : items) {
            amount=amount.add(item.getTotalPrice());
        }
        //减去优惠总价
        amount=amount.subtract(getReduce());
        return amount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
