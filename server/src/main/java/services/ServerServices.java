package services;

import java.util.ArrayList;
import java.util.List;

import daos.*;
import model.*;

public class ServerServices {

    private IDao<String, User> usersDao;
    private IDao<String, Group>  groupDao;
    
    public ServerServices() {
        this.usersDao = new UserDao();
        this.groupDao = new GroupDao();
    }

    synchronized public User registerUser(String name, boolean online) {
        User newUser = new User(name, online);

        if(usersDao.findAllKeys().contains(newUser.getName())){
            usersDao.update(newUser);
            System.out.println("The user " + newUser.getName() + " is already registered. Online status updated: online = " + newUser.isOnline());
        } else {
            usersDao.save(newUser);
            System.out.println("The new user " + newUser.getName() + " was registered");
        }

        return usersDao.finById(newUser.getName());
    }

    synchronized public List<String> getOnlineUsers() {
        System.out.println("Getting online users registered...");
        return usersDao.findAllKeys();
    }

    synchronized public List<Group> getAllGroups() {
        System.out.println("Getting all groups registered...");
        return groupDao.findAllValues();
    }

    synchronized public List<Group> getUserGroups(String username) {
        List<Group> allGroups = groupDao.findAllValues();

        System.out.println("Groups registered: " + allGroups.toString());

        List<Group> userGroups = new ArrayList<>();

        if (allGroups == null) {
            return userGroups;
        }

        for (Group group : allGroups) {
            for (String member : group.getMembers()) {
                if (member.equals(username)) {
                    userGroups.add(group);
                    break;
                }
            }
        }

        return userGroups;
    }

    synchronized public Group createGroup(String groupName, List<String> members) {
        Group newGroup = new Group(groupName, members);

        Group created = groupDao.save(newGroup);

        System.out.println("The group " + created.getName() + " has been created with members: " + created.getMembers().toString());

        return groupDao.finById(groupName);
    }
}
