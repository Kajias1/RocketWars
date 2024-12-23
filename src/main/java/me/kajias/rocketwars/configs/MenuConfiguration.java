package me.kajias.rocketwars.configs;

public class MenuConfiguration
{
    private static final BaseConfiguration menusConfig = new BaseConfiguration("menus");

    public static void initialize() { menusConfig.load(); }

    public static BaseConfiguration getMenuConfig() {
        return menusConfig;
    }
}
