package services;

import daos.IDao;
import daos.UserDao;
import model.User;

public class ServerServices {

    private IDao<String, User> usersDao;
    
    public ServerServices() {
        this.usersDao = new UserDao();
    }

    synchronized public User registerUser(User newUser) {
        if(usersDao.findAll().contains(newUser)){
            usersDao.update(newUser);
            System.out.println("The user " + newUser.getName() + "is already registered. Online status updated: online = " + newUser.isOnline());
        } else {
            usersDao.save(newUser);
            System.out.println("The new user " + newUser.getName() + "was registered");
        }

        return usersDao.finById(newUser.getName());
    }
}
