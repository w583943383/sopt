package com.sam.sopt.arithmetic;

import androidx.annotation.NonNull;

import java.util.List;

public class TTree<T extends Comparable> {


    public static class Node<T extends Comparable> {


        @NonNull
        private T data;
        public Node<T> left;
        public Node<T> right;

        public Node(@NonNull T data) {
            this.data = data;
        }

        public Node(@NonNull T data, Node<T> left, Node<T> right) {
            this.data = data;
            this.left = left;
            this.right = right;
        }

    }


    private Node<T> root;


    public TTree(Node<T> root) {
        this.root = root;
    }


    public void insert(@NonNull T data) {
        root = insert(data, root);
    }


    private Node<T> insert(T data, Node<T> node) {

        if (node == null) {
            node = new Node<>(data);
            return node;
        }

        int compareResult = data.compareTo(node.data);

        if (compareResult < 0) {
            node.left = insert(data, node.left);
        } else if (compareResult > 0) {
            node.right = insert(data, node.right);
        } else {
        }
        return node;

    }


    public boolean isEmpty() {
        return root == null;
    }


    public int size() {
        return size(root);
    }


    private int size(Node<T> node) {
        int size = 0;
        if (node != null) {
            size = size(node.left) + size(node.right) + 1;
        }
        return size;
    }

    public void remove(T data) {
        remove(data,root);
    }

    public void remove(T data,Node<T> node){
//        if(node){
//            return;
//        }
    }


    public void find(T data) {
        find(data, root);
    }


    public Node<T> find(T data, Node<T> node) {
        Node<T> result = null;
        if (node != null) {
            int comp = data.compareTo(node.data);
            if (comp == 0) {
                result = node;
            } else if (comp < 0) {
                result = find(data, node.left);
            } else if (comp > 0) {
                result = find(data, node.right);
            }
        }
        return result;
    }
}
