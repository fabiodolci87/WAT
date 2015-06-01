<?php

//return the list of the user

require("config.inc.php");

//initial query
$query = "Select * FROM positions WHERE groupID=:gruppo";

//Update query
    $query_params = array(
        ':gruppo' => $_POST['groupID']
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
        $post["username"] = $row["username"];
        $post["latitude"]    = $row["latitude"];
        $post["longitude"]  = $row["longitude"];
        
        array_push($response["posts"], $post);
    }
    
    echo json_encode($response);
    
} else {
    $response["success"] = 0;
    $response["message"] = "No Post Available!";
    die(json_encode($response));
}
?>

