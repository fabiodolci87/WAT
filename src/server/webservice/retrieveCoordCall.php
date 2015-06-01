<?php

// look if the coordinator has called to recompact the group

require("config.inc.php");

$query = "SELECT * FROM coord_calls WHERE groupID=:gruppo AND time >= NOW() - INTERVAL 1 MINUTE";

$query_params = array(
        ':gruppo' => $_POST['groupID']
        );

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
    $response["message"] = "Call Available!";
    $response["posts"]   = array();
    foreach ($rows as $row) {
        $post["longitude"]    = $row["longitude"];
        $post["latitude"]  = $row["latitude"];
        array_push($response["posts"], $post);
    }
    die(json_encode($response));
} else {
    $response["success"] = 0;
    $response["message"] = "No Call!";
    die(json_encode($response));
}