<?php
	$elements = array(
		array('name'=>'Light',    'unit'=>'Shining', 'canInflict'=>'Blind'  ),
		array('name'=>'Electric', 'unit'=>'Static',  'canInflict'=>'Neuro'  ),
		array('name'=>'Fire',     'unit'=>'Flaming', 'canInflict'=>'Burn'   ),
		array('name'=>'Frost',    'unit'=>'Frosty',  'canInflict'=>'Sleep'  ),
		array('name'=>'Sound',    'unit'=>'Sonic',   'canInflict'=>'Confuse'),
	);
	$weapons = array(
		array('name'=>'Blade', 'unit'=>'Swordsman', 'weakStatus'=>'Sleep',   'weakWeapon'=>'{"blade":.5 ,"blunt":.75,"spear":1.5,"whip":2  }'),
		array('name'=>'Blunt', 'unit'=>'Clubsman',  'weakStatus'=>'Burn',    'weakWeapon'=>'{"blade":1.5,"blunt":.5 ,"spear":2  ,"whip":.75}'),
		array('name'=>'Spear', 'unit'=>'Pikeman',   'weakStatus'=>'Blind',   'weakWeapon'=>'{"blade":2  ,"blunt":1.5,"spear":.5 ,"whip":1.5}'),
		array('name'=>'Whip',  'unit'=>'Whipman',   'weakStatus'=>'Confuse', 'weakWeapon'=>'{"blade":.75,"blunt":2  ,"spear":.75,"whip":.5 }'),
	);
	$directions = array('Down', 'Left', 'Up', 'Right');
	
?>
[	<?php
		foreach($elements as $element)
		foreach($weapons as $weapon)
		{
			echo('
	{
		"name":"'. $element['unit'] . ' ' . $weapon['unit'] . '",
		"element":"' . $element['name'] . '",
		"atkWeapon":"' . $weapon['name'] . '",
		"atkStatus":"'. $element['canInflict'] . '",
		"range":1,
		"speed":3,
		"body":"Human",
		"weakWeapon":' . $weapon['weakWeapon'] . ',
		"weakStatus":"' . $weapon['weakStatus'] . '",
		"weakDirection":"' . $directions[rand(0,3)] . '"
	},');
		}
	?>
	
]
