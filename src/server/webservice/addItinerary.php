<?php

//Used to add an Itinerary activity by the coordinator

require("config.inc.php");
	
	$query = "INSERT INTO itineraries (groupname, hour, object) VALUES (:groupname, :hour, :object) ";

    $query_params = array(
    	':groupname'=> $_POST['groupname'],
        ':hour' => $_POST['hour'],
        ':object' => $_POST['object']
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
    $response["groupname"] = $_POST['groupname'];
    echo json_encode($response);
?> 
