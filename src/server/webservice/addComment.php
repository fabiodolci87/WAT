<?php

//Add a comment to the database

require("config.inc.php");

	$query = "INSERT INTO comments ( username, title, message, groupID, name_pic) VALUES ( :user, :title, :message, :gruppo, :name_pic) ";

    $query_params = array(
        ':user' => $_POST['username'],
        ':title' => $_POST['title'],
		':message' => $_POST['message'],
        ':gruppo' => $_POST['groupID'],
        ':name_pic' => $_POST['name_pic']
    );

    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error. Couldn't add post!";
        die(json_encode($response));
    }

    $response["success"] = 1;
    $response["message"] = "Comment Successfully Added!";
    $response["gruppo"] = $_POST['groupID'];
    echo json_encode($response);  
?> 
