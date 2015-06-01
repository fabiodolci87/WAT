<?php

//load and connect to MySQL database stuff

require("config.inc.php");

    //gets user's info based off of a username.
    $query = " 
            SELECT  
                username, 
                password
            FROM users 
            WHERE 
                username = :username 
        ";
    
    $query_params = array(
        ':username' => $_POST['username']
    );
    
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error1. Please Try Again!";
        die(json_encode($response));
        
    }
   
    $validated_info = false;
    
    $row = $stmt->fetch();
    if ($row) {
        if (SHA1($_POST['password']) === $row['password']) {
            $login_ok = true;
        }
    }
    
    if ($login_ok) {
        $response["success"] = 1;
        $response["message"] = "Login successful!";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Invalid Credentials!";
        die(json_encode($response));
    }

?> 

