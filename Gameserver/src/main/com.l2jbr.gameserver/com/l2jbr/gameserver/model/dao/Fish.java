package com.l2jbr.gameserver.model.dao;

import com.l2jbr.commons.database.dao.DAO;

public class Fish implements DAO {
    private int id;
    private int level;
    private String name;
    private int hp;
    private int hpregen;
    private int fish_type;
    private int fish_group;
    private int fish_guts;
    private int guts_check_time;
    private int wait_time;
    private int combat_time;
}