package org.icesi.db;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/chat_app";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String[] tables = {
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id SERIAL PRIMARY KEY, " +
                            "username VARCHAR(50) UNIQUE NOT NULL, " +
                            "status VARCHAR(20) DEFAULT 'offline')",

                    "CREATE TABLE IF NOT EXISTS messages (" +
                            "id SERIAL PRIMARY KEY, " +
                            "sender_id INT REFERENCES users(id), " +
                            "recipient_id INT REFERENCES users(id), " +
                            "content TEXT NOT NULL, " +
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "is_group BOOLEAN DEFAULT false)",

                    "CREATE TABLE IF NOT EXISTS voice_messages (" +
                            "id SERIAL PRIMARY KEY, " +
                            "sender_id INT REFERENCES users(id), " +
                            "recipient_id INT REFERENCES users(id), " +
                            "audio_data BYTEA NOT NULL, " +
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "is_group BOOLEAN DEFAULT false)",

                    "CREATE TABLE IF NOT EXISTS groups (" +
                            "id SERIAL PRIMARY KEY, " +
                            "name VARCHAR(50) NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                    "CREATE TABLE IF NOT EXISTS group_members (" +
                            "group_id INT REFERENCES groups(id), " +
                            "user_id INT REFERENCES users(id), " +
                            "PRIMARY KEY(group_id, user_id))"
            };

            for (String sql : tables) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
            System.out.println("[DB] Base de datos inicializada correctamente");
        } catch (SQLException e) {
            System.err.println("[DB] Error inicializando BD: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // USUARIOS
    public int registerUser(String username) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO users(username) VALUES(?) ON CONFLICT(username) DO UPDATE SET status='online' RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error registrando usuario: " + e.getMessage());
        }
        return -1;
    }

    public String getUsernameById(int userId){
        try(Connection conn = getConnection()){
            String sql = "SELECT username FROM users WHERE id = ?";
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1,userId);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()){
                    return rs.getString("username");
                }
            }
        }catch(SQLException e){
            System.err.println("[DB] Error obteniendo usuario: " + e.getMessage());
        }
        return "";
    }

    public String getGroupnameById(int groupId){
        try(Connection conn = getConnection()){
            String sql = "SELECT name FROM groups WHERE id = ?";
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1,groupId);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()){
                    return rs.getString("name");
                }
            }
        }catch(SQLException e){
            System.err.println("[DB] Error obteniendo usuario: " + e.getMessage());
        }
        return "";
    }

    public void setUserStatus(int userId, String status) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE users SET status=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error actualizando estado: " + e.getMessage());
        }
    }

    // MENSAJES DE TEXTO
    public void saveMessage(int senderId, int recipientId, String content, boolean isGroup) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO messages(sender_id, recipient_id, content, is_group) VALUES(?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, senderId);
                stmt.setInt(2, recipientId);
                stmt.setString(3, content);
                stmt.setBoolean(4, isGroup);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error guardando mensaje: " + e.getMessage());
        }
    }

    // MENSAJES DE VOZ
    public void saveVoiceMessage(int senderId, int recipientId, byte[] audioData, boolean isGroup) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO voice_messages(sender_id, recipient_id, audio_data, is_group) VALUES(?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, senderId);
                stmt.setInt(2, recipientId);
                stmt.setBytes(3, audioData);
                stmt.setBoolean(4, isGroup);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error guardando mensaje de voz: " + e.getMessage());
        }
    }

    public byte[] getVoiceMessage(int messageId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT audio_data FROM voice_messages WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, messageId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getBytes("audio_data");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error obteniendo mensaje de voz: " + e.getMessage());
        }
        return null;
    }

    // GRUPOS
    public int createGroup(String groupName, int creatorId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            String groupSql = "INSERT INTO groups(name) VALUES(?) RETURNING id";
            int groupId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(groupSql)) {
                stmt.setString(1, groupName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    groupId = rs.getInt("id");
                }
            }

            if (groupId != -1) {
                String memberSql = "INSERT INTO group_members(group_id, user_id) VALUES(?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(memberSql)) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, creatorId);
                    stmt.executeUpdate();
                }
                conn.commit();
                return groupId;
            }
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("[DB] Error creando grupo: " + e.getMessage());
        }
        return -1;
    }

    public int getUserIdByUsername(String username) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT id FROM users WHERE username=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error buscando usuario: " + e.getMessage());
        }
        return -1;
    }

    public void addGroupMember(int groupId, int userId) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO group_members(group_id, user_id) VALUES(?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, groupId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error añadiendo miembro: " + e.getMessage());
        }
    }

    public void displayHistory(int userId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT u.username, m.content, m.timestamp FROM messages m " +
                    "JOIN users u ON m.sender_id = u.id WHERE m.recipient_id = ? OR m.sender_id = ? " +
                    "ORDER BY m.timestamp DESC LIMIT 20";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();
                System.out.println("\n=== HISTORIAL DE MENSAJES ===");
                boolean hasMessages = false;
                while (rs.next()) {
                    hasMessages = true;
                    System.out.println("[" + rs.getTimestamp("timestamp") + "] " +
                            rs.getString("username") + ": " + rs.getString("content"));
                }
                if (!hasMessages) {
                    System.out.println("No hay mensajes");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error mostrando historial: " + e.getMessage());
        }
    }

    public void listAllUsers(int currentUserId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, username, status FROM users WHERE id != ? ORDER BY username";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, currentUserId);
                ResultSet rs = stmt.executeQuery();
                System.out.println("\n╔════════════════════════════════╗");
                System.out.println("║      USUARIOS REGISTRADOS      ║");
                System.out.println("╚════════════════════════════════╝");
                boolean hasUsers = false;
                while (rs.next()) {
                    hasUsers = true;
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String status = rs.getString("status");
                    String statusIcon = status.equals("online") ? "🟢" : "⚫";
                    System.out.println(statusIcon + " [" + id + "] " + username + " (" + status + ")");
                }
                if (!hasUsers) {
                    System.out.println("No hay otros usuarios");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error listando usuarios: " + e.getMessage());
        }
    }

    public void listAllGroups() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, name FROM groups ORDER BY name";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                System.out.println("\n╔════════════════════════════════╗");
                System.out.println("║         GRUPOS DISPONIBLES     ║");
                System.out.println("╚════════════════════════════════╝");
                boolean hasGroups = false;
                while (rs.next()) {
                    hasGroups = true;
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    System.out.println("👥 [" + id + "] " + name);
                }
                if (!hasGroups) {
                    System.out.println("No hay grupos creados");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error listando grupos: " + e.getMessage());
        }
    }
}
