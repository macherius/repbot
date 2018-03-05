#!/usr/bin/perl -w

# upgrade a 0.5 database to 0.6 format

# input: a mysqldump of the old database
# output: sql script for creating the new database

print <<EOF;
create table users (
  id int auto_increment primary key,
  name varchar(255) not null unique
) type=InnoDB;

create table opinions (
  target_user_id int not null references users,
  originating_user_id int not null references users,
  originator_experience int not null,
  type char(1) not null,
  primary key (target_user_id, originating_user_id)
) type=InnoDB;

create index originators_idx on opinions ( originating_user_id );

EOF

sub sum(@) {
    my $sum = 0;
    for (@_) {
	$sum += $_;
    }
    return $sum;
}

sub round($) {
    my ($num) = @_;
    return int($num + ($num >= 0 ? 0.5 : -0.5));
}

$lastid = 1;

@spelling = ( "StoneTiger", "Zorba", "Patrixia", "SYFer", "HannsEisler", 
	      "DavidRogers", "tucsonAZ", "XCal", "MuffinHead", "LisaS", 
	      "philo", "Mikey", "Pipster", "Blot", "Biggles_two", "Nicki",
	      "TrixiB", "HotWired", "Jackson", "iceborg", "Maureen",
	      "SlickWill", "AramKhan", "NIHILBOT", "Gamsh", "Tequila",
	      "JerryW", "StrangeLuck", "Arina", "Denise", "BALBOA",
	      "Boogie", "fay_j", "LinkMasterJoe", "TomServo", "Slipshod" );

foreach (@spelling) { $spelling{lc $_} = $_ }

#print join " ", %spelling;

sub adduser($) {
    my ($who) = @_;
    $who = $spelling{lc $who} if $spelling{lc $who};
    unless ($users{$who}) {
	$users{$who} = $lastid++;
	print "INSERT INTO users VALUES ( $users{$who}, '$who' );\n";
    }
    return $users{$who};
}

while (<>) {
    $experience{$1} = $2 
	if /^INSERT INTO inputproviders VALUES \('(.*)',(\d+),(\d+),(\d+)\);*/;
    if (/^INSERT INTO users VALUES \('(.*)',',*(.*[^,]|),*',',*(.*[^,]|),*',(-?\d+)\);$/) {
	$user = $1;
	@complainers = split(/,/, $2);
	@vouchers = split(/,/, $3);
	$reputation = $4;
	#print "$user, @complainers, @vouchers, $reputation\n";

	next if $#complainers + $#vouchers == -2;

	$id = adduser($user);
	$updated_reputation = (sum map { $experience{$_} } @vouchers)
	                    - (sum map { $experience{$_} } @complainers);
	$factor = $updated_reputation != 0 ? 1.0 * $reputation / $updated_reputation : 0.0;
	
	%opinion = ();
	for $c (@complainers) { $opinion{$c} = -round($experience{$c} * $factor) }
	for $v (@vouchers)   { $opinion{$v} = +round($experience{$v} * $factor) }

	#print join(" ", %opinion), "\n";

	#$error = $reputation - sum values %opinion;
    
	for $o (keys %opinion) {
	    $who = adduser $o;
	    $how = $opinion{$o};
	    $type = $how >= 0 ? 'V' : 'C';
	    $how = abs $how;
	    print "INSERT INTO opinions VALUES ( $id, $who, $how, '$type' );\n";
	}

	#print "$user $factor $error\n";
	

    }
}

