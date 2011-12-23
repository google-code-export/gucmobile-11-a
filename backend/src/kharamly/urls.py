from django.conf.urls.defaults import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('backend.views',
    url(r'^api/(?P<orig>(.)+)/(?P<dest>(.)+)/(?P<speed>(.)+)/(?P<who>[\w\-_]+)$', 'api'),
    url(r'^test/(?P<test_value>(.)+)', 'test_method_in_views'),
    url(r'^getTwitterLoginInfo/(?P<user_name>(.)+)', 'getTwitterLoginInfo'),
    url(r'^checkUserExists/(?P<user_name>(.)+)', 'checkUserExists'),
    url(r'^test_evaluate/(?P<origin>(.)+)/(?P<destination>(.)+)/([a-z]+)/([a-z]+)/$', 'route_blockage'),
    url(r'^directions/(?P<origin>(.)+)/(?P<destination>(.)+)/$', 'directions'),
    url(r'^inRadius/(?P<longitude>(.)+)/(?P<latitude>(.)+)/(?P<radius>(.)+)/$', 'inRadius'),
    url(r'^alternatives/(?P<location>\d+)/(?P<destination>\d+)$', 'alternatives'),
    url(r'^update/(?P<stepId>\d+)/(?P<routeId>\d+)/(?P<speed>\d+)$', 'alternatives'),
    url(r'^admin/', include(admin.site.urls)),
)
