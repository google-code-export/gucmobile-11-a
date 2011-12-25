from django.conf.urls.defaults import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('backend.views',
    url(r'rate_comment/(?P<comment_id>\d)/(?P<rate>\d)$')
    url(r'^api/(?P<orig>(.)+)/(?P<dest>(.)+)/(?P<speed>(.)+)/(?P<who>[\w\-_]+)$', 'api'),
    url(r'^test/(?P<test_value>(.)+)', 'test_method_in_views'),
    url(r'^getTwitterLoginInfo/(?P<user_name>(.)+)', 'getTwitterLoginInfo'),
    url(r'^checkUserExists/(?P<user_name>(.)+)', 'checkUserExists'),
    url(r'^saveTwitterUserInfo/(?P<user_name>(.)+)/(?P<token>(.)+)/(?P<secret>(.)+)', 'saveTwitterUserInfo'),
    url(r'^test_evaluate/(?P<origin>(.)+)/(?P<destination>(.)+)/([a-z]+)/([a-z]+)/$', 'route_blockage'),
    url(r'^directions/(?P<origin>(.)+)/(?P<destination>(.)+)/$', 'directions'),
    url(r'^update/(?P<stepId>\d+)/(?P<routeId>\d+)/(?P<speed>\d+)$', 'alternatives'),
    url(r'^admin/', include(admin.site.urls)),
)
