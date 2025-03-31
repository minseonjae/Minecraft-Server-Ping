package kr.codingtree;

import kr.codingtree.mcserverping.MinecraftServerPing;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        System.out.println(MinecraftServerPing.getServerPing("leafserver.kr", 25565, 1000, 4, false));
    }
}