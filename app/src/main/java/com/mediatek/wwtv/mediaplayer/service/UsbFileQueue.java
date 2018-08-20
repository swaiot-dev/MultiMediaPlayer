package com.mediatek.wwtv.mediaplayer.service;

import java.util.LinkedList;


/**
 * 队列数据结构的模拟,用来实现队列的先进先出操作,并判断是否为空
 *
 * @param <E> 队列中存储数据的类型
 * @author rs
 * @version 1.0
 */
public class UsbFileQueue<E> {

    private LinkedList<E> queue;

    public UsbFileQueue() {
        queue = new LinkedList<E>();
    }

    public void add(E e) {
        queue.addFirst(e);
    }

    public E remove() {
        return queue.removeFirst();
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }
}

