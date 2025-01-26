package kr.codingtree;

import kr.codingtree.mcserverping.MCServerPing;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        System.out.println(MCServerPing.getServerPing("leafserver.kr", 25565, 1000, 4));
    }
}