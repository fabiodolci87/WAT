<?php

//add a new user to the database

require("config.inc.php");

    if (empty($_POST['username']) || empty($_POST['password'])) {
        
        $response["success"] = 0;
        $response["message"] = "Please Enter Both a Username and Password.";
        
        die(json_encode($response));
    }
    
    $query        = " SELECT 1 FROM users WHERE username = :user";
    $query_params = array(':user' => $_POST['username']);
    
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error1. Please Try Again!";
        die(json_encode($response));
    }
    
    $row = $stmt->fetch();
    if ($row) {
        $response["success"] = 0;
        $response["message"] = "I'm sorry, this username is already in use";
        die(json_encode($response));
    }
    
    $query = "INSERT INTO users ( username, password ) VALUES ( :user, SHA(:pass) ) ";
    
    $query_params = array(
        ':user' => $_POST['username'],
        ':pass' => $_POST['password']
    );
    
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error2. Please Try Again!";
        die(json_encode($response));
    }
    
    $response["success"] = 1;
    $response["message"] = "Username Successfully Added!";
    echo json_encode($response);
    
?>
