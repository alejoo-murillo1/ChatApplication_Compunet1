package org.icesi.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

                    "CREATE TABLE IF NOT EXISTS groups (" +
                            "id SERIAL PRIMARY KEY, " +
                            "name VARCHAR(50) NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                    "CREATE TABLE IF NOT EXISTS group_members (" +
                            "group_id INT REFERENCES groups(id) ON DELETE CASCADE, " +
                            "user_id INT REFERENCES users(id) ON DELETE CASCADE, " +
                            "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "PRIMARY KEY(group_id, user_id))",

                    "CREATE TABLE IF NOT EXISTS private_messages (" +
                            "id SERIAL PRIMARY KEY, " +
                            "sender_id INT REFERENCES users(id) ON DELETE CASCADE, " +
                            "recipient_id INT REFERENCES users(id) ON DELETE CASCADE, " +
                            "content TEXT NOT NULL, " +
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                    "CREATE TABLE IF NOT EXISTS group_messages (" +
                            "id SERIAL PRIMARY KEY, " +
                            "sender_id INT REFERENCES users(id) ON DELETE CASCADE, " +
                            "group_id INT REFERENCES groups(id) ON DELETE CASCADE, " +
                            "content TEXT NOT NULL, " +
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                    "CREATE TABLE IF NOT EXISTS private_voice_messages (" +
                            "id SERIAL PRIMARY KEY, " +
                            "sender_id INT REFERENCES users(id) ON DELETE CASCADE, " +
                            "recipient_id INT REFERENCES users(id) ON DELETE CASCADE, " +
                            "audio_data BYTEA NOT NULL, " +
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                    "CREATE TABLE IF NOT EXISTS group_voice_messages (" +
                            "id SERIAL PRIMARY KEY, " +
                            "sender_id INT REFERENCES users(id) ON DELETE CASCADE, " +
                            "group_id INT REFERENCES groups(id) ON DELETE CASCADE, " +
                            "audio_data BYTEA NOT NULL, " +
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
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

    public String getUsernameById(int userId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT username FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error obteniendo usuario: " + e.getMessage());
        }
        return "";
    }

    public String getGroupnameById(int groupId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT name FROM groups WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, groupId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error obteniendo grupo: " + e.getMessage());
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
    public void savePrivateMessage(int senderId, int recipientId, String content) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO private_messages(sender_id, recipient_id, content) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, senderId);
                stmt.setInt(2, recipientId);
                stmt.setString(3, content);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error guardando mensaje privado: " + e.getMessage());
        }
    }

    public void displayAudioHistory(int userId) {
        try (Connection conn = getConnection()) {
            String audioSql = "SELECT 'PRIVADO_AUDIO' as type, u.username as sender_name, " +
                    "NULL as content, pvm.timestamp, NULL as group_name, pvm.id as audio_id " +
                    "FROM private_voice_messages pvm " +
                    "JOIN users u ON pvm.sender_id = u.id " +
                    "WHERE pvm.recipient_id = ? OR pvm.sender_id = ? " +
                    "UNION ALL " +
                    "SELECT 'GRUPO_AUDIO' as type, u.username as sender_name, " +
                    "NULL as content, gvm.timestamp, g.name as group_name, gvm.id as audio_id " +
                    "FROM group_voice_messages gvm " +
                    "JOIN users u ON gvm.sender_id = u.id " +
                    "JOIN groups g ON gvm.group_id = g.id " +
                    "JOIN group_members gmem ON g.id = gmem.group_id " +
                    "WHERE gmem.user_id = ? " +
                    "ORDER BY timestamp DESC LIMIT 20";

            try (PreparedStatement stmt = conn.prepareStatement(audioSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                stmt.setInt(3, userId);
                ResultSet rs = stmt.executeQuery();

                System.out.println("\n=== HISTORIAL DE AUDIOS ===");
                boolean hasAudios = false;
                while (rs.next()) {
                    hasAudios = true;
                    String type = rs.getString("type");
                    String sender = rs.getString("sender_name");
                    String timestamp = rs.getTimestamp("timestamp").toString();
                    String groupName = rs.getString("group_name");
                    int audioId = rs.getInt("audio_id");

                    if ("PRIVADO_AUDIO".equals(type)) {
                        System.out.println("[" + timestamp + "] " + sender + " (audio privado) - ID: " + audioId);
                    } else {
                        System.out.println("[" + timestamp + "] " + sender + " @" + groupName + " (audio) - ID: " + audioId);
                    }
                }
                if (!hasAudios) {
                    System.out.println("No hay mensajes de audio");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error mostrando historial de audios: " + e.getMessage());
        }
    }

    public void saveGroupMessage(int senderId, int groupId, String content) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO group_messages(sender_id, group_id, content) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, senderId);
                stmt.setInt(2, groupId);
                stmt.setString(3, content);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error guardando mensaje grupal: " + e.getMessage());
        }
    }

    // MENSAJES DE VOZ
    public void savePrivateVoiceMessage(int senderId, int recipientId, byte[] audioData) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO private_voice_messages(sender_id, recipient_id, audio_data) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, senderId);
                stmt.setInt(2, recipientId);
                stmt.setBytes(3, audioData);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error guardando mensaje de voz privado: " + e.getMessage());
        }
    }

    public void saveGroupVoiceMessage(int senderId, int groupId, byte[] audioData) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO group_voice_messages(sender_id, group_id, audio_data) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, senderId);
                stmt.setInt(2, groupId);
                stmt.setBytes(3, audioData);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error guardando mensaje de voz grupal: " + e.getMessage());
        }
    }

    public byte[] getPrivateVoiceMessage(int messageId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT audio_data FROM private_voice_messages WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, messageId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getBytes("audio_data");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error obteniendo mensaje de voz privado: " + e.getMessage());
        }
        return null;
    }

    public byte[] getGroupVoiceMessage(int messageId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT audio_data FROM group_voice_messages WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, messageId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getBytes("audio_data");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error obteniendo mensaje de voz grupal: " + e.getMessage());
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

    // MÉTODOS AUXILIARES NUEVOS
    public boolean isUserInGroup(int userId, int groupId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT 1 FROM group_members WHERE user_id = ? AND group_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, groupId);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error verificando membresía: " + e.getMessage());
        }
        return false;
    }

    public List<Integer> getGroupMembers(int groupId) {
        List<Integer> members = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT user_id FROM group_members WHERE group_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, groupId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    members.add(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error obteniendo miembros: " + e.getMessage());
        }
        return members;
    }

    // HISTORIAL MEJORADO
    public void displayHistory(int userId) {
        try (Connection conn = getConnection()) {
            String privateSql = "SELECT 'PRIVADO' as type, u.username as sender_name, " +
                    "pm.content, pm.timestamp, NULL as group_name " +
                    "FROM private_messages pm " +
                    "JOIN users u ON pm.sender_id = u.id " +
                    "WHERE pm.recipient_id = ? OR pm.sender_id = ? " +
                    "UNION ALL " +
                    "SELECT 'GRUPO' as type, u.username as sender_name, " +
                    "gm.content, gm.timestamp, g.name as group_name " +
                    "FROM group_messages gm " +
                    "JOIN users u ON gm.sender_id = u.id " +
                    "JOIN groups g ON gm.group_id = g.id " +
                    "JOIN group_members gmem ON g.id = gmem.group_id " +
                    "WHERE gmem.user_id = ? " +
                    "ORDER BY timestamp DESC LIMIT 20";

            try (PreparedStatement stmt = conn.prepareStatement(privateSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                stmt.setInt(3, userId);
                ResultSet rs = stmt.executeQuery();

                System.out.println("\n=== HISTORIAL DE MENSAJES ===");
                boolean hasMessages = false;
                while (rs.next()) {
                    hasMessages = true;
                    String type = rs.getString("type");
                    String sender = rs.getString("sender_name");
                    String content = rs.getString("content");
                    String timestamp = rs.getTimestamp("timestamp").toString();
                    String groupName = rs.getString("group_name");

                    if ("PRIVADO".equals(type)) {
                        System.out.println("[" + timestamp + "] " + sender + " (privado): " + content);
                    } else {
                        System.out.println("[" + timestamp + "] " + sender + " @" + groupName + ": " + content);
                    }
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
