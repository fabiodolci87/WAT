<?php

//used to retrieve the itinerary

require("config.inc.php");

//initial query
$query = "Select * FROM itineraries WHERE groupname=:groupname ORDER by hour ASC ";

//Update query
    $query_params = array(
        ':groupname' => $_POST['groupname']
        );

//execute query
try {
    $stmt   = $db->prepare($query);
    $result = $stmt->execute($query_params);
}
catch (PDOException $ex) {
    $response["success"] = 0;
    $response["message"] = "Database Error!";
    die(json_encode($response));
}

$rows = $stmt->fetchAll();

if ($rows) {
    $response["success"] = 1;
    $response["message"] = "Post Available!";
    $response["posts"]   = array();
    
    foreach ($rows as $row) {
        $post["groupname"] = $row["groupname"];
        $post["hour"]    = $row["hour"];
        $post["object"]  = $row["object"];
        
        array_push($response["posts"], $post);
    }
    echo json_encode($response);
    
} else {
    $response["success"] = 0;
    $response["message"] = "No Post Available!";
    die(json_encode($response));
}
?>

