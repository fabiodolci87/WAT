<?php

// compute the location of the group finding the barycenter

$servername = "127.0.0.1";
$username = "root";
$password = "";
$dbname = "my_watlocate";

$gruppo = $_POST["groupID"];
$username = $_POST["username"];
$timestamp = time();


$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

/////////////////////////////////////////////////////////////////////////////////////
$sql_del = "DELETE FROM `positions` WHERE coordinator<>1 AND ($timestamp-lastUP>900)";
$result_del = $conn->query($sql_del);
/////////////////////////////////////////////////////////////////////////////////////

$sql_med="SELECT count(*) from positions where groupID = '$gruppo'";
$result_med = $conn->query($sql_med);
if ($result_med->num_rows > 0) {
    while($row = $result_med->fetch_assoc()) {
    $c=$row["count(*)"];
    }
} else {
    echo "0 results";
}

$index = floor($c / 2);

$sql_med1="select latitude,username from positions where groupID = '$gruppo' order by latitude asc limit $index,1";
$result_med1 = $conn->query($sql_med1);
if ($result_med1->num_rows > 0) {
    while($row = $result_med1->fetch_assoc()) {
    $latAVG=$row["latitude"];
    $user_median=$row["username"];
    }
} else {
    echo "0 results";
}

//FIND MEDIAN LONGITUDE

$sql_med1="select longitude from positions where username='$user_median'";
$result_med1 = $conn->query($sql_med1);
if ($result_med1->num_rows > 0) {
    while($row = $result_med1->fetch_assoc()) {
    $longAVG=$row["longitude"];
    $response["success"] = 1;
    $response["median"] = $user_median;
    $response["latAVG"] =  $latAVG;
	$response["longAVG"] =  $longAVG;
    $response["users_active"] = $c;
    echo json_encode($response);
    }
} else {
    echo "0 results";
}

$conn->close();
?> 