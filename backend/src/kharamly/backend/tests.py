"""
This file demonstrates writing tests using the unittest module. These will pass
when you run "manage.py test".

Replace this with more appropriate tests for your application.

Possible Assertion Methods:

assertEqual(a, b)   a == b   
assertNotEqual(a, b)    a != b   
assertTrue(x)   bool(x) is True  
assertFalse(x)  bool(x) is False     
assertIs(a, b)  a is b  
assertIsNot(a, b)   a is not b  
assertIsNone(x) x is None   
assertIsNotNone(x)  x is not None   
assertIn(a, b)  a in b  
assertNotIn(a, b)   a not in b  
assertIsInstance(a, b)  isinstance(a, b)    
assertNotIsInstance(a, b)   not isinstance(a, b) 

assertRaises(exc, fun, *args, **kwds)   fun(*args, **kwds) raises exc    
assertRaisesRegexp(exc, re, fun, *args, **kwds) fun(*args, **kwds) raises exc and the message matches re 

assertAlmostEqual(a, b) round(a-b, 7) == 0   
assertNotAlmostEqual(a, b)  round(a-b, 7) != 0   
assertGreater(a, b) a > b   
assertGreaterEqual(a, b)    a >= b  
assertLess(a, b)    a < b   
assertLessEqual(a, b)   a <= b  
assertRegexpMatches(s, re)  regex.search(s) 
assertNotRegexpMatches(s, re)   not regex.search(s) 
assertItemsEqual(a, b)  sorted(a) == sorted(b) and works with unhashable objs   
assertDictContainsSubset(a, b)  all the key/value pairs in a exist in b 

assertMultiLineEqual(a, b)  strings 
assertSequenceEqual(a, b)   sequences   
assertListEqual(a, b)   lists   
assertTupleEqual(a, b)  tuples  
assertSetEqual(a, b)    sets or frozensets  
assertDictEqual(a, b)   dicts   
"""

from django.test import TestCase
from backend.models import *
from random import *

class SimpleTest(TestCase):
    def test_basic_addition(self):
        """
        Tests that 1 + 1 always equals 2.
        """
        self.assertEqual(1 + 1, 2)

######################### BADGE TESTS #########################
class BadgeTest(TestCase):
    fixtures = ['devices']
    def setUp(self):
        self.device = Device.objects.get(id=1)
        self.speed = self.to_mps(40)


    def test_speed_badge_handler_returns_badge(self):
        """
        Tests if the speed_badge_handler method returns the expected badge
        Author: Shanab
        """
        for speed in xrange(100,181,40):
            self.speed = self.to_mps(speed)
            badge = speed_badge_handler(self.device, self.speed)
            self.assertEqual(badge, Badge.objects.get(name="speedster", value=speed))

    def test_speed_badge_handler_saves_badge(self):
        """
        Tests if the speed_badge_handler method saves the relation between badge
        and user in the join table
        Author: Shanab
        """
        for speed in xrange(100,181,40):
            self.speed = self.to_mps(speed)
            speed_badge_handler(self.device, self.speed)
            self.assertIn(Badge.objects.get(name="speedster", value=speed), self.device.badge_set.all())

    def test_speed_badge_handler_saves_at_most_three_badges(self):
        """
        Tests that the user acquires at most three non-repeated badges,
        regardless of how many times the user exceeds the badge speed requirement
        """
        for i in xrange(0,11):
            self.speed = randint(120,240)
            speed_badge_handler(self.device, self.speed)
            self.assertLessEqual(self.device.badge_set.count(), 3)

    def test_badger_badge_handler_returns_and_saves_badge(self):
        """
        Tests if the badger_badge_handler returns the badger badge
        and saves it in the database when the user acquires all other
        badges
        """
        all_badges = Badge.objects.all()
        for i in xrange(1,20):
            self.device.badge_set.add(all_badges[i])
        badge = badger_badge_handler(self.device)
        self.assertEqual(badge, Badge.objects.get(id=20))
        self.assertIn(Badge.objects.get(name="badger"), self.device.badge_set.all())
    
    ######################### TEST HELPERS #########################

    def to_mps(self, speed_in_kph):
        """
        Return:
            Speed in meters per second
        Arguments:
            speed_in_kph: speed in kilometers per hour
        Author: Shanab
        """
        return math.ceil(speed_in_kph * 1000 / 60 / 60.0)

###################### END OF BADGE TESTS ######################